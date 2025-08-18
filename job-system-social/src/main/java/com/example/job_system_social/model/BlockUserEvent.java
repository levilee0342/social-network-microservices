package com.example.job_system_social.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockUserEvent {
    private String userId;
    private String reason;
    private Long detectedAt;
}

