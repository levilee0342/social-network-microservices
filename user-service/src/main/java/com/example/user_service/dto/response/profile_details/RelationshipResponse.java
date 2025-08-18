package com.example.user_service.dto.response.profile_details;

import lombok.Data;

import java.util.Date;

@Data
public class RelationshipResponse {
    private String status;
    private String partnerName;
    private String startDate;
    private Boolean isPublic;
}
