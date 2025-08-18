package com.example.post_service.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class UserTypePreferenceRequest {
    private List<Long> typeIds;
}
