package com.example.trending_service.utils;

import org.springframework.stereotype.Component;

@Component
public class ParseUtils {
    public long parseLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
