package com.example.post_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInteractionEventRequest {
    private String userId;
    private List<Long> typeIds;
    private String eventType; // comment / like / share
    private Long timestamp;
}
