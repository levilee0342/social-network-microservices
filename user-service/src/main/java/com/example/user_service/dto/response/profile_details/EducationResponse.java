package com.example.user_service.dto.response.profile_details;

import lombok.Data;

@Data
public class EducationResponse {
    private String schoolName;
    private String major;
    private Integer startYear;
    private Integer endYear;
    private Boolean graduate;
    private String description;
    private Boolean isPublic;
}
