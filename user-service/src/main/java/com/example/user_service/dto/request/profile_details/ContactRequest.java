package com.example.user_service.dto.request.profile_details;

import com.example.user_service.entity.PublicField;
import lombok.Data;

import java.util.Map;

@Data
public class ContactRequest {
    private PublicField<String> phone;
    private PublicField<String> email;
    private PublicField<String> website;
    private Map<String, PublicField<String>> socialLinks;
}
