package com.example.user_service.dto.response.profile_details;

import lombok.Data;

@Data
public class FamilyMemberResponse {
    private String userId;
    private String relationship;
    private Boolean isPublic;
}
