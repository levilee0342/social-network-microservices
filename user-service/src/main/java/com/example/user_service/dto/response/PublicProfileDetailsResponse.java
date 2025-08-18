package com.example.user_service.dto.response;

import com.example.user_service.dto.response.profile_details.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PublicProfileDetailsResponse {
    private String profileDetailsId;
    private String userId;

    private List<EducationResponse> education;
    private List<WorkResponse> work;
    private LocationResponse location;
    private ContactResponse contact;
    private RelationshipResponse relationship;
    private List<FamilyMemberResponse> familyMembers;
    private DetailResponse detail;

    private String createdAt;
    private String updatedAt;
}
