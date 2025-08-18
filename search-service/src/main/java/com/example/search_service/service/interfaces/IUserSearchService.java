package com.example.search_service.service.interfaces;

import com.example.search_service.dto.response.UserResponse;
import java.util.List;

public interface IUserSearchService {
    List<UserResponse> searchUsers(String query, String userId, int limit, int offset);
}
