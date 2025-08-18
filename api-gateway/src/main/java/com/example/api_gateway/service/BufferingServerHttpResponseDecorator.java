package com.example.api_gateway.service;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

public class BufferingServerHttpResponseDecorator extends ServerHttpResponseDecorator {

    private final AtomicReference<String> fullBody = new AtomicReference<>("");

    public BufferingServerHttpResponseDecorator(ServerHttpResponse delegate) {
        super(delegate);
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        if (body instanceof Flux) {
            Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;

            // Join toàn bộ nội dung response lại để đọc và ghi
            return DataBufferUtils.join(fluxBody)
                    .flatMap(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);

                        String responseContent = new String(content, StandardCharsets.UTF_8);
                        fullBody.set(responseContent);

                        // Ghi lại buffer gốc vào response
                        DataBuffer bufferToWrite = bufferFactory().wrap(content);
                        return getDelegate().writeWith(Mono.just(bufferToWrite));
                    });
        }

        return super.writeWith(body);
    }

    public String getFullBody() {
        return fullBody.get();
    }
}
