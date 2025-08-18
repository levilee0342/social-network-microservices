package com.example.post_service.service.impl;

import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.entity.Post;
import com.example.post_service.entity.SearchPostIndex;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.service.interfaces.ISearchPostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SearchPostServiceImpl implements ISearchPostService {

    private final RestHighLevelClient restHighLevelClient;
    private final ProducerLog logProducer;

    public SearchPostServiceImpl(RestHighLevelClient restHighLevelClient,
                                 ProducerLog logProducer){
        this.restHighLevelClient = restHighLevelClient;
        this.logProducer = logProducer;
    }

    @Override
    @Transactional
    public List<PostResponse> searchPostsByQuery(String query, String userId, int limit, int offset, HttpServletRequest httpRequest) {
        try {
            String decodedQuery = URLDecoder.decode(query, StandardCharsets.UTF_8);
            logProducer.sendLog("post-service", "INFO", "[Post] Searching posts (raw) with query: '" + query + "', userId: " + userId);
            return searchByQueryTextRaw(decodedQuery, limit, offset);
        } catch (Exception e) {
            throw new AppException(AuthErrorCode.FAILED_SEARCH_POST);
        }
    }

    private List<PostResponse> searchByQueryTextRaw(String queryText, int limit, int offset) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> allowedVisibilities = List.of("PUBLIC", "FRIENDS_ONLY", "ANONYMOUS");

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.multiMatchQuery(queryText)
                            .field("title")
                            .field("content"))
                    .filter(QueryBuilders.termsQuery("visibility", allowedVisibilities));

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                    .query(boolQuery)
                    .from(offset)
                    .size(limit)
                    .sort("createdAt", SortOrder.DESC);

            SearchRequest searchRequest = new SearchRequest("posts").source(sourceBuilder);
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            List<PostResponse> results = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                SearchPostIndex index = objectMapper.readValue(hit.getSourceAsString(), SearchPostIndex.class);
                results.add(mapFromSearchIndex(index));
            }

            return results;
        } catch (IOException e) {
            throw new AppException(AuthErrorCode.FAILED_SEARCH_ELASTICSEARCH);
        }
    }

    @Override
    @Transactional
    public List<PostResponse> searchPostsByHashtag(String hashtag, String userId, int limit, int offset, HttpServletRequest httpRequest) {
        try {
            logProducer.sendLog("post-service", "INFO", "[Post] Searching posts (raw) with hashtag: '" + hashtag + "', userId: " + userId);
            return searchByHashtagInElasticRaw(hashtag, limit, offset);
        } catch (Exception e) {
            logProducer.sendLog("post-service", "ERROR", "[Post] Failed to search hashtag in Elasticsearch: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_SEARCH_ELASTICSEARCH);
        }
    }

    private List<PostResponse> searchByHashtagInElasticRaw(String hashtag, int limit, int offset) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> allowedVisibilities = List.of("PUBLIC", "FRIENDS_ONLY", "ANONYMOUS");

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("hashtags", hashtag))
                    .filter(QueryBuilders.termsQuery("visibility", allowedVisibilities));

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
                    .query(boolQuery)
                    .from(offset)
                    .size(limit)
                    .sort("createdAt", SortOrder.DESC);

            SearchRequest searchRequest = new SearchRequest("posts").source(sourceBuilder);
            SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            List<PostResponse> results = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                SearchPostIndex index = objectMapper.readValue(hit.getSourceAsString(), SearchPostIndex.class);
                results.add(mapFromSearchIndex(index));
            }

            return results;
        } catch (IOException e) {
            throw new AppException(AuthErrorCode.FAILED_SEARCH_ELASTICSEARCH);
        }
    }

    private PostResponse mapFromSearchIndex(SearchPostIndex index) {
        return PostResponse.builder()
                .postId(index.getPostId())
                .userId(index.getUserId())
                .title(index.getTitle())
                .content(index.getContent())
                .hashtags(index.getHashtags())
                .createdAt(index.getCreatedAt())
                .visibility(Post.PostVisibility.valueOf(index.getVisibility()))
                .build();
    }
}
