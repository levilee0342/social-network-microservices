package com.example.user_service.service.interfaces;

import com.example.user_service.dto.response.ProfileResponse;

import java.util.List;

public interface ISearchProfileService {
    List<ProfileResponse> searchProfiles(String fullName, String address, String phone);
}
