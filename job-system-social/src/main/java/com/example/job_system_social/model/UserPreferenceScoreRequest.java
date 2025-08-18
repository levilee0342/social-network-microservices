package com.example.job_system_social.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceScoreRequest {
    private String userId;
    private Long typeId;
    private Integer score;
}
