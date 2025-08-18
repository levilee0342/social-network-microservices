package com.example.user_service.dto.request.profile_details;

import com.example.user_service.entity.PublicField;
import lombok.Data;

import java.util.List;

@Data
public class DetailRequest {
    private PublicField<String> quote;
    private PublicField<String> nickname;
    private PublicField<String> bloodType;
    private PublicField<String> religion;
    private List<PublicField<String>> languages;
}
