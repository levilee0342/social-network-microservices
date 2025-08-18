package com.example.user_service.dto.request.profile_details;

import lombok.Data;

@Data
public class FamilyMemberRequest {
    private String userId;
    private String relationship;
    private Boolean isPublic;
}
