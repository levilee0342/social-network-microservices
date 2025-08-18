package com.example.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final WebClient webClient;

    @Value("${identity.service.url}")
    private String identityServiceUrl;

    public JwtAuthenticationFilter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        // Bỏ qua filter cho /register và /login
        if (path.startsWith("/identity/register")
                || path.startsWith("/identity/login")
                || path.startsWith("/identity/request-otp")
                || path.startsWith("/identity/oauth2/authorization")
                || path.startsWith("/api/login/oauth2/code")
                || path.startsWith("/oauth2/authorization")) {
            return chain.filter(exchange);
        }

        // Xử lý /identity/validate
        if (path.equals("/identity/validate")) {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("Missing or invalid Authorization header for /identity/validate");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            String token = authHeader.substring(7);
            System.out.println("Validating token for /identity/validate: " + token);
            return webClient.get()
                    .uri(identityServiceUrl)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(userId -> {
                        System.out.println("Validated user_id: " + userId);
                        // Lưu user_id vào response header (hoặc body) để client nhận
                        exchange.getResponse().getHeaders().add("X-User-Id", userId);
                        // Lưu user_id vào SecurityContext cho các yêu cầu khác
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userId, null, null);
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                    })
                    .onErrorResume(e -> {
                        System.out.println("Validation error: " + e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        }

        // Xác thực cho các endpoint khác
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Validating token: " + token);
            return webClient.get()
                    .uri(identityServiceUrl)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(userId -> {
                        System.out.println("Validated user_id: " + userId);
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userId, null, null);
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                    })
                    .onErrorResume(e -> {
                        System.out.println("Validation error: " + e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        }
        // ws-chat
        if (path.startsWith("/ws-chat/info")
                || path.matches("^/ws-chat/[^/]+/xhr.*")
                || path.matches("^/ws-chat/[^/]+/websocket.*")
                || path.equals("/ws-chat")) {
            return chain.filter(exchange);
        }
        System.out.println("No Authorization header, proceeding without authentication");
        return chain.filter(exchange);
    }
}
