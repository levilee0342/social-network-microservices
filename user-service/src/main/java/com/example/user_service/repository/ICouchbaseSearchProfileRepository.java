package com.example.user_service.repository;

import com.example.user_service.dto.response.ProfileResponse;

import java.util.List;

public interface ICouchbaseSearchProfileRepository {
    List<ProfileResponse> search(String fullName, String address, String phone);
}


