package com.example.search_service.service.impl;

import com.example.search_service.dto.response.ApiResponse;
import com.example.search_service.dto.response.PostResponse;
import com.example.search_service.service.interfaces.IPostSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Service
public class PostSearchServiceImpl implements IPostSearchService {
    private static final Logger logger = LoggerFactory.getLogger(PostSearchServiceImpl.class);
    private final RestTemplate restTemplate;

    public PostSearchServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<PostResponse> searchPosts(String query, String userId, int limit, int offset, String authorizationHeader) {
        return fetchPostsFromUrl(
                String.format("http://post-service/posts/search?query=%s&limit=%d&offset=%d",
                        UriUtils.encode(query, StandardCharsets.UTF_8),
                        limit,
                        offset),
                authorizationHeader
        );
    }

    @Override
    public List<PostResponse> searchPostsByHashtag(String hashtag, String userId, int limit, int offset, String authorizationHeader) {
        return fetchPostsFromUrl(
                String.format("http://post-service/posts/search-by-hashtag?hashtag=%s&limit=%d&offset=%d",
                        UriUtils.encode(hashtag, StandardCharsets.UTF_8),
                        limit,
                        offset),
                authorizationHeader
        );
    }

    private List<PostResponse> fetchPostsFromUrl(String url, String authorizationHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authorizationHeader);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ParameterizedTypeReference<ApiResponse<List<PostResponse>>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<List<PostResponse>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    responseType
            );
            ApiResponse<List<PostResponse>> apiResponse = response.getBody();
            if (apiResponse != null && apiResponse.getResult() != null) {
                logger.info("Số lượng bài viết trả về: {}", apiResponse.getResult().size());
                return apiResponse.getResult();
            } else {
                logger.warn("API trả về null hoặc rỗng.");
                return Collections.emptyList();
            }
        } catch (Exception e) {
            logger.error("Lỗi khi gọi Post Service tại URL [{}]: {}", url, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
