package com.example.message_service.interceptor;

import com.example.message_service.utils.JwtUtil;
import com.example.message_service.utils.WebSocketSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private WebSocketSessionRegistry sessionRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            // Nếu không có Authorization header, lấy từ query param của URI
            if (token == null) {
                Object connectMsgObj = accessor.getHeader("simpConnectMessage");
                if (connectMsgObj instanceof Message) {
                    Message<?> connectMsg = (Message<?>) connectMsgObj;
                    Object uriObj = connectMsg.getHeaders().get("uri");
                    if (uriObj instanceof URI) {
                        URI uri = (URI) uriObj;
                        String query = uri.getQuery();
                        if (query != null && query.contains("token=")) {
                            String[] params = query.split("&");
                            for (String param : params) {
                                if (param.startsWith("token=")) {
                                    token = URLDecoder.decode(param.substring(6), StandardCharsets.UTF_8);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (token != null) {
                if (token.startsWith("Bearer ")) token = token.substring(7);
                String userId = jwtUtil.getUsernameFromToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                accessor.setUser(authentication);
                sessionRegistry.register(accessor.getSessionId(), userId);
            }
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            sessionRegistry.unregister(accessor.getSessionId());
        }
        return message;
    }


}


