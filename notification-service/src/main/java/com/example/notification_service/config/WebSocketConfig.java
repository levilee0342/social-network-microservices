package com.example.notification_service.config;

import com.example.notification_service.dto.response.ApiResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebClient.Builder webClientBuilder;
    private final ServiceProperties serviceProperties;

    public WebSocketConfig(WebClient.Builder webClientBuilder, ServiceProperties serviceProperties) {
        this.webClientBuilder = webClientBuilder;
        this.serviceProperties = serviceProperties;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-notifications").setAllowedOriginPatterns("http://localhost:5173").withSockJS(); // Endpoint WebSocket
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && "CONNECT".equalsIgnoreCase(accessor.getCommand().name())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            WebClient webClient = webClientBuilder.build();
                            ApiResponse<String> apiResponse = webClient.get()
                                    .uri(serviceProperties.getIdentity().getUrl())
                                    .header("Authorization", "Bearer " + token)
                                    .retrieve()
                                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {})
                                    .block();
                            if (apiResponse != null && apiResponse.getCode() == 200) {
                                String userId = apiResponse.getResult();
                                Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, null);
                                accessor.setUser(auth);
                            } else {
                                throw new RuntimeException("Invalid token");
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Token validation failed", e);
                        }
                    } else {
                        throw new RuntimeException("No Bearer token found");
                    }
                }
                return message;
            }
        });
    }
}