package com.example.post_service.service;

import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.service.interfaces.ISearchPostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostSearchService {

    private final ISearchPostService searchPostService;

    public PostSearchService(ISearchPostService searchPostService) {
        this.searchPostService = searchPostService;
    }

    public List<PostResponse> searchPostsByQuery(String query, String userId, int limit, int offset, HttpServletRequest httpRequest) {
        return searchPostService.searchPostsByQuery(query, userId, limit, offset, httpRequest);
    }

    public List<PostResponse> searchPostsByHashtag(String hashtag, String userId, int limit, int offset, HttpServletRequest httpRequest) {
        return searchPostService.searchPostsByHashtag(hashtag, userId, limit, offset, httpRequest);
    }
}
