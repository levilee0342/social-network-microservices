package com.example.search_service.service.interfaces;

import com.example.search_service.dto.response.PostResponse;
import com.example.search_service.dto.response.UserResponse;
import java.util.List;

public interface ISearchResultSaver {
    void saveUserSearchResult(String query, String userId, List<UserResponse> users);
    void savePostSearchResult(String query, String userId, List<PostResponse> posts);
}
