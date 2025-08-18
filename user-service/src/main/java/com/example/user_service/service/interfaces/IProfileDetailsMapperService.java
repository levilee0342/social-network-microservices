package com.example.user_service.service.interfaces;

import com.couchbase.client.java.json.JsonObject;
import com.example.user_service.dto.response.PublicProfileDetailsResponse;

public interface IProfileDetailsMapperService {
    PublicProfileDetailsResponse mapToResponseForOwner(PublicProfileDetailsResponse profile);
    PublicProfileDetailsResponse mapJsonToProfileDetails(JsonObject row);
    PublicProfileDetailsResponse mapToResponse(PublicProfileDetailsResponse profile);
}
