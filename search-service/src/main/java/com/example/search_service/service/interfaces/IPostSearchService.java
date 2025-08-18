package com.example.search_service.service.interfaces;

import com.example.search_service.dto.response.PostResponse;
import java.util.List;

public interface IPostSearchService {
    List<PostResponse> searchPosts(String query, String userId, int limit, int offset, String authorizationHeader);
    List<PostResponse> searchPostsByHashtag(String hashtag, String userId, int limit, int offset, String authorizationHeader);
}
