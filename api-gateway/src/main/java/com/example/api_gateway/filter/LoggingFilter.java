package com.example.api_gateway.filter;

import com.example.api_gateway.entity.RequestLog;
import com.example.api_gateway.repository.RequestLogRepository;
import com.example.api_gateway.service.BufferingServerHttpResponseDecorator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

@Component
@RequiredArgsConstructor
public class LoggingFilter implements WebFilter {

    private final RequestLogRepository requestLogRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().toString();
        String path = exchange.getRequest().getPath().toString();

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    byte[] bodyBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bodyBytes);
                    DataBufferUtils.release(dataBuffer);
                    String requestBody = new String(bodyBytes, StandardCharsets.UTF_8);

                    Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bodyBytes);
                        return Mono.just(buffer);
                    });

                    ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return cachedFlux;
                        }
                    };

                    BufferingServerHttpResponseDecorator decoratedResponse =
                            new BufferingServerHttpResponseDecorator(exchange.getResponse());

                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(mutatedRequest)
                            .response(decoratedResponse)
                            .build();

                    return ReactiveSecurityContextHolder.getContext()
                            .map(ctx -> ctx.getAuthentication() != null ? ctx.getAuthentication().getName() : "anonymous")
                            .defaultIfEmpty("anonymous")
                            .flatMap(userId -> chain.filter(mutatedExchange).then(Mono.defer(() -> {
                                long endTime = System.currentTimeMillis();
                                int responseTime = (int) (endTime - startTime);
                                int status = mutatedExchange.getResponse().getStatusCode() != null
                                        ? mutatedExchange.getResponse().getStatusCode().value()
                                        : 500;

                                String responseBody = decoratedResponse.getFullBody();

                                RequestLog log = RequestLog.builder()
                                        .method(method)
                                        .path(path)
                                        .status(status)
                                        .userId(userId)
                                        .responseTime(responseTime)
                                        .createdAt(new Timestamp(System.currentTimeMillis()))
                                        .requestBody(requestBody)
                                        .responseBody(responseBody)
                                        .build();

                                return Mono.fromCallable(() -> requestLogRepository.save(log))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .then();
                            })));
                })
                .switchIfEmpty(chain.filter(exchange).then(Mono.defer(() -> {
                    long endTime = System.currentTimeMillis();
                    int responseTime = (int) (endTime - startTime);
                    int status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 500;

                    // Không ép kiểu exchange.getResponse() vì không bọc decorator trong switchIfEmpty
                    String responseBody = ""; // hoặc để null nếu cần

                    return ReactiveSecurityContextHolder.getContext()
                            .map(ctx -> ctx.getAuthentication() != null ? ctx.getAuthentication().getName() : "anonymous")
                            .defaultIfEmpty("anonymous")
                            .flatMap(userId -> {
                                RequestLog log = RequestLog.builder()
                                        .method(method)
                                        .path(path)
                                        .status(status)
                                        .userId(userId)
                                        .responseTime(responseTime)
                                        .createdAt(new Timestamp(System.currentTimeMillis()))
                                        .requestBody("")
                                        .responseBody(responseBody)
                                        .build();

                                return Mono.fromCallable(() -> requestLogRepository.save(log))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .then();
                            });
                })));
    }
}
