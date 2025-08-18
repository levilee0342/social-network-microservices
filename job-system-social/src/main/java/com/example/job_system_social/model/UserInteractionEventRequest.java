package com.example.job_system_social.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInteractionEventRequest {
    private String userId;
    private Long typeId;
    private List<Long> typeIds;
    private String eventType; // comment / like / share
    private Long timestamp;
}
