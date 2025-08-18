package com.example.post_service.utils;

import com.example.post_service.entity.SearchPostIndex;
import com.example.post_service.kafka.producer.ProducerLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class IndexPostUtils {

    private final RestHighLevelClient restHighLevelClient;
    private final ProducerLog logProducer;

    public IndexPostUtils(RestHighLevelClient restHighLevelClient, ProducerLog logProducer) {
        this.restHighLevelClient = restHighLevelClient;
        this.logProducer = logProducer;
    }

    public void indexPostsBulk(List<SearchPostIndex> indices) {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        ObjectMapper objectMapper = new ObjectMapper();

        for (SearchPostIndex index : indices) {
            try {
                Map<String, Object> source = objectMapper.convertValue(index, Map.class);
                IndexRequest request = new IndexRequest("posts")
                        .id(String.valueOf(index.getPostId()))
                        .source(source);
                bulkRequest.add(request);
            } catch (Exception e) {
                logProducer.sendLog("post-service", "ERROR", "[Post] Failed to prepare index for postId: " + index.getPostId());
            }
        }

        try {
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("Bulk index failed", e);
        }
    }
}
