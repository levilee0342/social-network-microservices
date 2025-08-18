package com.example.search_service.service.interfaces;

import com.example.search_service.dto.response.SearchResult;

public interface ISearchService {
    SearchResult search(String query, String type, int limit, int offset, String authorizationHeader);
}
