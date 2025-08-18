package com.example.user_service.dto.request.profile_details;

import lombok.Data;

import java.util.Date;

@Data
public class RelationshipRequest {
    private String status;
    private String partnerName;
    private String startDate;
    private Boolean isPublic;
}
