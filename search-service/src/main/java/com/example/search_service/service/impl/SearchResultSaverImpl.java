package com.example.search_service.service.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.example.search_service.dto.response.PostResponse;
import com.example.search_service.dto.response.UserResponse;
import com.example.search_service.service.interfaces.ISearchResultSaver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class SearchResultSaverImpl implements ISearchResultSaver {
    private final Collection searchPostCollection;
    private final Collection searchUserCollection;
    private final Collection searchQueryCollection;

    public SearchResultSaverImpl(@Qualifier("searchBucket") Bucket searchBucket) {
        this.searchPostCollection = searchBucket.scope("search_post_result").collection("post");
        this.searchUserCollection = searchBucket.scope("search_user_result").collection("user");
        this.searchQueryCollection = searchBucket.scope("search_query_result").collection("query");
    }

    @Override
    public void saveUserSearchResult(String query, String userId, List<UserResponse> users) {
        try {
            saveQuery(query, userId);
            for (UserResponse user : users) {
                String docId = user.getUserId();
                JsonObject existingDoc;
                try {
                    GetResult getResult = searchUserCollection.get(docId);
                    existingDoc = getResult.contentAsObject();
                    int currentCount = existingDoc.getInt("searchCount");
                    existingDoc.put("searchCount", currentCount + 1);
                } catch (Exception e) {
                    existingDoc = JsonObject.create()
                            .put("userId", docId)
                            .put("searchCount", 1);
                }
                searchUserCollection.upsert(docId, existingDoc);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void savePostSearchResult(String query, String userId, List<PostResponse> posts) {
        try {
            saveQuery(query, userId);
            for (PostResponse post : posts) {
                String docId = post.getPostId();
                JsonObject existingDoc;
                try {
                    GetResult getResult = searchPostCollection.get(docId);
                    existingDoc = getResult.contentAsObject();
                    int currentCount = existingDoc.getInt("searchCount");
                    existingDoc.put("searchCount", currentCount + 1);
                } catch (Exception e) {
                    existingDoc = JsonObject.create()
                            .put("postId", docId)
                            .put("searchCount", 1);
                }
                searchPostCollection.upsert(docId, existingDoc);
            }
        } catch (Exception ignored) {
        }
    }

    private void saveQuery(String query, String userId) {
        try {
            JsonObject queryDoc = JsonObject.create()
                    .put("query", query)
                    .put("userId", userId)
                    .put("timestamp", Instant.now().toString());

            String docId = UUID.randomUUID().toString();
            searchQueryCollection.insert(docId, queryDoc);
        } catch (Exception ignored) {
        }
    }
}
