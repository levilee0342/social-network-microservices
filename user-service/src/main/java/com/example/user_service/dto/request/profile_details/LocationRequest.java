package com.example.user_service.dto.request.profile_details;

import com.example.user_service.entity.PublicField;
import lombok.Data;

@Data
public class LocationRequest {
    private PublicField<String> currentCity;
    private PublicField<String> hometown;
}
