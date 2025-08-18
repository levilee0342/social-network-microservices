package com.example.message_service.filters;

import com.example.message_service.utils.LoggingUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Order(1)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private final LoggingUtil loggingUtil;

    public RequestResponseLoggingFilter(LoggingUtil loggingUtil) {
        this.loggingUtil = loggingUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.nanoTime();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            // Sau khi xử lý thành công
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
            String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

            Map<String, Object> logMap = buildLogMap(request, response, requestBody, responseBody, duration);
            loggingUtil.logInfo("user-service", "[API MESSAGE-SERVICE] Request/Response: " + toJson(logMap), logMap);

        } catch (Exception ex) {
            // Xử lý lỗi
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);

            Map<String, Object> errorLogMap = buildLogMap(request, response, requestBody, "", duration);
            errorLogMap.put("error", ex.getMessage());

            loggingUtil.logError("user-service", "[API MESSAGE-SERVICE] ERROR during request", errorLogMap, ex);
            throw ex; // Rethrow để không chặn flow
        } finally {
            // Ghi nội dung response về lại client
            wrappedResponse.copyBodyToResponse();
        }
    }

    private Map<String, Object> buildLogMap(HttpServletRequest request, HttpServletResponse response,
                                            String requestBody, String responseBody, long duration) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("method", request.getMethod());
        map.put("uri", request.getRequestURI());
        map.put("query", request.getQueryString());
        map.put("client_ip", request.getRemoteAddr());
        map.put("request_body", requestBody);
        map.put("status", response.getStatus());
        map.put("response_body", responseBody);
        map.put("duration_ms", duration);
        return map;
    }

    private String toJson(Map<String, Object> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (Exception e) {
            return map.toString();
        }
    }
}
