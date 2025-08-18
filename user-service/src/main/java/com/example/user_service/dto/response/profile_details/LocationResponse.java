package com.example.user_service.dto.response.profile_details;

import com.example.user_service.entity.PublicField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private PublicField<String> currentCity;
    private PublicField<String> hometown;
}
