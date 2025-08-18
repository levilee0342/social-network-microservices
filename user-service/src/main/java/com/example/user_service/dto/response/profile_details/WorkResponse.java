package com.example.user_service.dto.response.profile_details;

import com.example.user_service.dto.request.profile_details.WorkRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkResponse {
    private String company;
    private String position;
    private String startDate;
    private String endDate;
    private String description;
    private Boolean isPublic;
}
