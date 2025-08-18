package com.example.user_service.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document
@Data
public class ProfileDetails {
    @Id
    private String profileDetailsId;

    private String userId;
    private List<Education> education;
    private List<Work> work;
    private Location location;
    private Contact contact;
    private Relationship relationship;
    private List<FamilyMember> familyMembers;
    private Detail detail;

    private Date createdAt;
    private Date updatedAt;

    @Data
    public static class Education {
        private String schoolName;
        private String major;
        private Integer startYear;
        private Integer endYear;
        private Boolean graduate;
        private String description;
        private Boolean isPublic;
    }

    @Data
    public static class Work {
        private String company;
        private String position;
        private Date startDate;
        private Date endDate;
        private String description;
        private Boolean isPublic;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private PublicField<String> currentCity;
        private PublicField<String> hometown;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        private PublicField<String> phone;
        private PublicField<String> email;
        private PublicField<String> website;
        private Map<String, PublicField<String>> socialLinks;
    }

    @Data
    public static class Relationship {
        private String status;
        private String partnerName;
        private Date startDate;
        private Boolean isPublic;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {
        private PublicField<String> quote;
        private PublicField<String> nickname;
        private PublicField<String> bloodType;
        private PublicField<String> religion;
        private List<PublicField<String>> languages;
    }

    @Data
    public static class FamilyMember {
        private String userId;
        private String relationship;
        private Boolean isPublic;
    }
}
