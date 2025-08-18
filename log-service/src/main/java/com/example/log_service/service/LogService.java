package com.example.log_service.service;

import com.example.log_service.entity.Log;
import com.example.log_service.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LogService {

    @Autowired
    private LogRepository logRepository;

    @Transactional
    public Log saveLog(Log log) {
        if (log == null) {
            throw new IllegalArgumentException("Log cannot be null");
        }
        if (log.getServiceName() == null || log.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Service name is required");
        }
        if (log.getLogLevel() == null || !isValidLogLevel(log.getLogLevel())) {
            throw new IllegalArgumentException("Invalid log level");
        }
        if (log.getMessage() == null || log.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Log message is required");
        }
        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }
        return logRepository.save(log);
    }

    @Transactional
    public Page<Log> findLogs(String serviceName, String logLevel, String keyword, Pageable pageable) {
        serviceName = trimInput(serviceName);
        logLevel = trimInput(logLevel);
        keyword = trimInput(keyword);
        return logRepository.findLogs(serviceName, logLevel, keyword, pageable);
    }

    @Transactional
    public Map<String, Long> getLogStats(String serviceName, String logLevel, String keyword) {
        serviceName = trimInput(serviceName);
        logLevel = trimInput(logLevel);
        keyword = trimInput(keyword);

        Map<String, Long> stats = new HashMap<>();
        if (logLevel != null) {
            if (!isValidLogLevel(logLevel)) {
                throw new IllegalArgumentException("Invalid log level: " + logLevel);
            }
            stats.put(logLevel.toUpperCase(), logRepository.countByLevel(logLevel.toUpperCase(), serviceName, keyword));
        } else {
            List<Object[]> counts = logRepository.countByAllLevels(serviceName, keyword);
            for (Object[] result : counts) {
                String level = ((String) result[0]).toUpperCase();
                Long count = ((Number) result[1]).longValue();
                stats.put(level, count);
            }
            // Đảm bảo tất cả các log level được bao gồm, ngay cả khi count = 0
            stats.putIfAbsent("ERROR", 0L);
            stats.putIfAbsent("WARN", 0L);
            stats.putIfAbsent("INFO", 0L);
            stats.putIfAbsent("DEBUG", 0L);
        }
        return stats;
    }

    private String trimInput(String input) {
        if (input != null && !input.trim().isEmpty()) {
            input = input.trim();
            if (input.length() > 255) {
                throw new IllegalArgumentException("Input is too long");
            }
            return input;
        }
        return null;
    }

    private boolean isValidLogLevel(String logLevel) {
        return logLevel != null && Arrays.asList("ERROR", "WARN", "INFO", "DEBUG").contains(logLevel.toUpperCase());
    }
}