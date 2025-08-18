package com.example.search_service.controller;

import com.example.search_service.dto.response.SearchResult;
import com.example.search_service.service.SearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public SearchResult search(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        return searchService.search(query, type, limit, offset, authorizationHeader);
    }

}
