package com.example.user_service.dto.response.profile_details;

import com.example.user_service.entity.PublicField;
import lombok.Data;

import java.util.Map;

@Data
public class ContactResponse {
    private PublicField<String> phone;
    private PublicField<String> email;
    private PublicField<String> website;
    private Map<String, PublicField<String>> socialLinks;
}
