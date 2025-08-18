package com.example.search_service.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class SearchResult {
    private List<UserResponse> users;
    private List<PostResponse> posts;
    private int totalCount;
}
