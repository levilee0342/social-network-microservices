package com.example.message_service.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GraphQLError {
    private String message;
    private List<Object> path;
    private Map<String, Object> extensions;
}
