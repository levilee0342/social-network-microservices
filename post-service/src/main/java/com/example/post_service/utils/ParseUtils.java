package com.example.post_service.utils;

import com.example.post_service.dto.response.CommentResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.sql.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class ParseUtils {

    private final ObjectMapper objectMapper;

    public ParseUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<CommentResponse> parseJsonList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public List<String> parseStringArray(Array sqlArray) {
        try {
            return Arrays.asList((String[]) sqlArray.getArray());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
