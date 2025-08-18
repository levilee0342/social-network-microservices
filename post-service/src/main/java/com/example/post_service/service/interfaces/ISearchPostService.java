package com.example.post_service.service.interfaces;

import com.example.post_service.dto.response.PostResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ISearchPostService {
    List<PostResponse> searchPostsByQuery(String query, String userId, int limit, int offset, HttpServletRequest httpRequest);
    List<PostResponse> searchPostsByHashtag(String hashtag, String userId, int limit, int offset, HttpServletRequest httpRequest);
}
