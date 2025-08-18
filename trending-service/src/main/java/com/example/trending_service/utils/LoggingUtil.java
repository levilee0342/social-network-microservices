package com.example.trending_service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LoggingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtil.class);

    public void logInfo(String service, String message, Map<String, Object> additionalFields) {
        setMdc(additionalFields);
        LOGGER.info(message);
        MDC.clear();
    }

    public void logError(String service, String messageTemplate, Map<String, Object> additionalFields, Throwable ex) {
        setMdc(additionalFields);
        LOGGER.error(messageTemplate, ex);
        MDC.clear();
    }

    private void setMdc(Map<String, Object> fields) {
        if (fields != null) {
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                MDC.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
    }
}

