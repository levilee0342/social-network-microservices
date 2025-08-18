package com.example.search_service.service;

import com.example.search_service.dto.response.SearchResult;
import com.example.search_service.service.interfaces.*;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final ISearchService searchService;

    public SearchService(ISearchService searchService) {
        this.searchService = searchService;
    }

    public SearchResult search(String query, String type, int limit, int offset, String authorizationHeader){
        return searchService.search(query, type, limit, offset, authorizationHeader);
    }
}