package com.example.notification_service.filters;

import com.example.notification_service.utils.LoggingUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Aspect
@Component
public class KafkaEventLoggingAspect {

    private final LoggingUtil loggingUtil;
    private final ObjectMapper objectMapper;

    public KafkaEventLoggingAspect(LoggingUtil loggingUtil) {
        this.loggingUtil = loggingUtil;
        this.objectMapper = new ObjectMapper();
    }

    @Around("execution(* com.example.notification_service.service.impl.*.consume*(..))")
    public Object logKafkaEvent(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("⚠️ Aspect triggered for Kafka consume method");
        long startTime = System.nanoTime();
        Object message = joinPoint.getArgs()[0];

        Map<String, Object> logMap = new LinkedHashMap<>();
        logMap.put("source", "kafka");
        logMap.put("message_class", message.getClass().getSimpleName());
        logMap.put("message_content", toJson(message));
        logMap.put("method", joinPoint.getSignature().toShortString());

        try {
            Object result = joinPoint.proceed();
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            logMap.put("status", "success");
            logMap.put("duration_ms", duration);

            loggingUtil.logInfo("notification-service", "[KafkaConsumer] Successfully processed event", logMap);
            return result;
        } catch (Exception e) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            logMap.put("status", "error");
            logMap.put("duration_ms", duration);
            logMap.put("error", e.getMessage());

            loggingUtil.logError("notification-service", "[KafkaConsumer] Failed to process event", logMap, e);
            throw e;
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
