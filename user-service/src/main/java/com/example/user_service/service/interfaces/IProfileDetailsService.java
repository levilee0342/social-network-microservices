package com.example.user_service.service.interfaces;

import com.example.user_service.dto.request.profile_details.*;
import com.example.user_service.dto.response.PublicProfileDetailsResponse;
import com.example.user_service.dto.response.profile_details.*;

public interface IProfileDetailsService {
    PublicProfileDetailsResponse findByUserId(String userId);
    PublicProfileDetailsResponse getOwnProfile(String userId);
    PublicProfileDetailsResponse createDefaultProfileDetails(String userId);

    EducationResponse addEducation(String userId, EducationRequest request);
    EducationResponse updateEducationByIndex(String userId, int index, EducationRequest request);
    boolean deleteEducationByIndex(String userId, int index);

    WorkResponse addWork(String userId, WorkRequest request);
    WorkResponse updateWorkByIndex(String userId, int index, WorkRequest request);
    boolean deleteWorkByIndex(String userId, int index);

    FamilyMemberResponse addFamilyMember(String userId, FamilyMemberRequest request);
    FamilyMemberResponse updateFamilyMemberByIndex(String userId, int index, FamilyMemberRequest request);
    boolean deleteFamilyMemberByIndex(String userId, int index);

    LocationResponse updateLocation(String userId, LocationRequest request);
    boolean deleteLocationField(String userId, String fieldToDelete);

    ContactResponse updateContact(String userId, ContactRequest request);
    boolean deleteContactField(String userId, String fieldName, String socialKey);

    RelationshipResponse updateRelationship(String userId, RelationshipRequest request);
    boolean deleteRelationship(String userId);

    DetailResponse updateDetail(String userId, DetailRequest request);
    boolean deleteDetailField(String userId, String fieldToDelete);
}
