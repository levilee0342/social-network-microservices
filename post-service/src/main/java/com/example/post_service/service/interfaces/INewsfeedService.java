package com.example.post_service.service.interfaces;

import com.example.post_service.dto.response.PostResponse;

import java.util.List;

public interface INewsfeedService {
    List<PostResponse> getNewsfeed(String userId, List<String> friendIds, int limit, int offset);
}
