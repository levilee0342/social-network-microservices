package com.example.user_service.service.interfaces;

import com.couchbase.client.java.json.JsonObject;
import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.dto.response.PublicProfileResponse;

public interface IProfileMapperService {
    ProfileResponse mapToResponse(ProfileResponse profile);
    ProfileResponse mapJsonToProfile(JsonObject row);
    PublicProfileResponse mapToPublicResponse(ProfileResponse profile);
}