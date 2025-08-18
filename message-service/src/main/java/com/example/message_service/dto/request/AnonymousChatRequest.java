package com.example.message_service.dto.request;

import lombok.Data;

@Data
public class AnonymousChatRequest {
    private String userId;
    private boolean gender;
    private Integer birthYear;
    private String address;
    private Long timestamp;
    private boolean matched;
}
