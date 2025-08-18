package com.example.search_service.service.impl;

import com.example.search_service.dto.response.SearchResult;
import com.example.search_service.service.interfaces.*;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements ISearchService {
    private final IUserValidator userValidator;
    private final IUserSearchService userSearchService;
    private final IPostSearchService postSearchService;
    private final ISearchResultSaver searchResultSaver;

    public SearchServiceImpl(IUserValidator userValidator, IUserSearchService userSearchService,
                         IPostSearchService postSearchService, ISearchResultSaver searchResultSaver) {
        this.userValidator = userValidator;
        this.userSearchService = userSearchService;
        this.postSearchService = postSearchService;
        this.searchResultSaver = searchResultSaver;
    }

    public SearchResult search(String query, String type, int limit, int offset, String authorizationHeader) {
        String userId = userValidator.validateUser();
        SearchResult result = new SearchResult();
        if ("ALL".equalsIgnoreCase(type) || "USER".equalsIgnoreCase(type)) {
            var users = userSearchService.searchUsers(query, userId, limit, offset);
            result.setUsers(users);
            searchResultSaver.saveUserSearchResult(query, userId, users);
        }
        if ("ALL".equalsIgnoreCase(type) || "POST".equalsIgnoreCase(type)) {
            var posts = postSearchService.searchPosts(query, userId, limit, offset, authorizationHeader);
            result.setPosts(posts);
            searchResultSaver.savePostSearchResult(query, userId, posts);
        }
        if ("HASHTAG".equalsIgnoreCase(type)) {
            var posts = postSearchService.searchPostsByHashtag(query, null, limit, offset, authorizationHeader);
            result.setPosts(posts);
            searchResultSaver.savePostSearchResult(query, userId, posts);
        }
        result.setTotalCount(
                (result.getUsers() != null ? result.getUsers().size() : 0) +
                        (result.getPosts() != null ? result.getPosts().size() : 0)
        );
        return result;
    }
}
