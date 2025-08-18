package com.example.user_service.controller;

import com.example.user_service.dto.request.profile_details.*;
import com.example.user_service.dto.response.profile_details.*;
import com.example.user_service.dto.response.PublicProfileDetailsResponse;
import com.example.user_service.service.interfaces.IProfileDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;

@Controller
@RequiredArgsConstructor
public class ProfileDetailsController {

    private final IProfileDetailsService profileDetailsService;

    @QueryMapping
    public PublicProfileDetailsResponse getProfileDetails(Authentication authentication) {
        String userId = authentication.getName();
        return profileDetailsService.getOwnProfile(userId);
    }

    @QueryMapping
    public PublicProfileDetailsResponse getPublicProfileDetails(@Argument String userId) {
        return profileDetailsService.findByUserId(userId);
    }

    @MutationMapping
    public PublicProfileDetailsResponse createDefaultProfileDetails(Authentication authentication) {
        String userId = authentication.getName();
        return profileDetailsService.createDefaultProfileDetails(userId);
    }

    @MutationMapping
    public EducationResponse addEducation(
            Authentication authentication,
            @Argument("request") EducationRequest request) {
        String userId = authentication.getName();
        return profileDetailsService.addEducation(userId, request);
    }

    @MutationMapping(name = "updateEducation")
    public EducationResponse updateEducationByIndex(
            Authentication authentication,
            @Argument int index,
            @Argument("request") EducationRequest request) {
        String userId = authentication.getName();
        System.out.println("Authenticated user ID: " + userId);
        return profileDetailsService.updateEducationByIndex(userId, index, request);
    }

    @MutationMapping(name = "deleteEducation")
    public Boolean deleteEducationByIndex(
            Authentication authentication,
            @Argument int index) {
        String userId = authentication.getName();
        return profileDetailsService.deleteEducationByIndex(userId, index);
    }

    // --------- Work ----------
    @MutationMapping
    public WorkResponse addWork(
            Authentication authentication,
            @Argument("request") WorkRequest request) {
        String userId = authentication.getName();
        return profileDetailsService.addWork(userId, request);
    }

    @MutationMapping(name = "updateWork")
    public WorkResponse updateWorkByIndex(
            Authentication authentication,
            @Argument int index,
            @Argument("request") WorkRequest request) {
        String userId = authentication.getName();
        return profileDetailsService.updateWorkByIndex(userId, index, request);
    }

    @MutationMapping(name = "deleteWork")
    public Boolean deleteWorkByIndex(
            Authentication authentication,
            @Argument int index) {
        String userId = authentication.getName();
        return profileDetailsService.deleteWorkByIndex(userId, index);
    }

    // --------- Family Member ----------
    @MutationMapping
    public FamilyMemberResponse addFamilyMember(
            Authentication authentication,
            @Argument("request") FamilyMemberRequest request) {
        String userId = authentication.getName();
        return profileDetailsService.addFamilyMember(userId, request);
    }

    @MutationMapping(name = "updateFamilyMember")
    public FamilyMemberResponse updateFamilyMemberByIndex(
            Authentication authentication,
            @Argument int index,
            @Argument("request") FamilyMemberRequest request) {
        String userId = authentication.getName();
        return profileDetailsService.updateFamilyMemberByIndex(userId, index, request);
    }

    @MutationMapping(name = "deleteFamilyMember")
    public Boolean deleteFamilyMemberByIndex(
            Authentication authentication,
            @Argument int index) {
        String userId = authentication.getName();
        return profileDetailsService.deleteFamilyMemberByIndex(userId, index);
    }

    // --------- Location ----------
    @MutationMapping
    public LocationResponse updateLocation(
            Authentication authentication,
            @Argument("request") LocationRequest request) {
        String userId = authentication.getName();
        return profileDetailsService.updateLocation(userId, request);
    }

    @MutationMapping
    public Boolean deleteLocationField(
            Authentication authentication,
            @Argument String fieldName
    ) {
        return profileDetailsService.deleteLocationField(authentication.getName(), fieldName);
    }

    // --------- Contact ----------
    @MutationMapping
    public ContactResponse updateContact(
            Authentication authentication,
            @Argument("request") ContactRequest request) {
        String userId = authentication.getName();
        return profileDetailsService.updateContact(userId, request);
    }

    @MutationMapping
    public Boolean deleteContactField(
            Authentication authentication,
            @Argument String fieldName,
            @Argument String socialKey
    ) {
        return profileDetailsService.deleteContactField(authentication.getName(), fieldName, socialKey);
    }

    // --------- Relationship ----------
    @MutationMapping
    public RelationshipResponse updateRelationship(
            Authentication authentication,
            @Argument("request") RelationshipRequest request) {
        String userId = authentication.getName();
        return profileDetailsService.updateRelationship(userId, request);
    }

    @MutationMapping
    public Boolean deleteRelationship(Authentication authentication) {
        String userId = authentication.getName();
        return profileDetailsService.deleteRelationship(userId);
    }

    // --------- Detail ----------
    @MutationMapping
    public DetailResponse updateDetail(
            Authentication authentication,
            @Argument("request") DetailRequest request) {
        String userId = authentication.getName();
        return profileDetailsService.updateDetail(userId, request);
    }

    @MutationMapping
    public Boolean deleteDetailField(
            Authentication authentication,
            @Argument String fieldName
    ) {
        return profileDetailsService.deleteDetailField(authentication.getName(), fieldName);
    }

}

