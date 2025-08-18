package com.example.search_service.service.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.search.SearchOptions;
import com.couchbase.client.java.search.queries.BooleanQuery;
import com.couchbase.client.java.search.queries.MatchQuery;
import com.couchbase.client.java.search.result.SearchRow;
import com.example.search_service.dto.response.UserResponse;
import com.example.search_service.service.interfaces.IUserSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserSearchServiceImpl implements IUserSearchService {
    private static final Logger logger = LoggerFactory.getLogger(UserSearchServiceImpl.class);
    private final Cluster cluster;
    private final Collection profileCollection;

    public UserSearchServiceImpl(@Qualifier("profileCouchbaseCluster") Cluster cluster,
                                 @Qualifier("profileBucket") Bucket profileBucket) {
        this.cluster = cluster;
        this.profileCollection = profileBucket.scope("user_information").collection("profiles");
    }

    @Override
    public List<UserResponse> searchUsers(String query, String userId, int limit, int offset) {
        try {
            // Tạo truy vấn tìm kiếm
            BooleanQuery booleanQuery = new BooleanQuery()
                    .should(
                            new MatchQuery(query).field("fullName"),
                            new MatchQuery(query).field("address")
                    );
            SearchOptions searchOptions = SearchOptions.searchOptions()
                    .limit(limit)
                    .skip(offset);

            // Thực hiện tìm kiếm
            var result = cluster.searchQuery("profile_bucket.user_information.user_index", booleanQuery, searchOptions);

            // Lấy danh sách ID từ kết quả tìm kiếm
            List<String> ids = result.rows().stream()
                    .map(SearchRow::id)
                    .toList();

            if (ids.isEmpty()) {
                logger.info("No search results found for query: {}", query);
                return Collections.emptyList();
            }

            // Truy xuất tài liệu từ collection
            return ids.stream()
                    .map(id -> {
                        try {
                            GetResult getResult = profileCollection.get(id);
                            JsonObject content = getResult.contentAsObject();

                            // Map to UserResponse
                            UserResponse response = new UserResponse();
                            response.setUserId(content.getString("userId"));
                            response.setFullName(content.getString("fullName"));
                            response.setEmail(content.getString("email"));
                            response.setPhone(content.getString("phone"));
                            response.setAddress(content.getString("address"));
                            // Handle dob as epoch milliseconds (Long)
                            Object dobObj = content.get("dateOfBirth");
                            if (dobObj instanceof Long) {
                                response.setDob(new Date((Long) dobObj));
                            } else {
                                logger.warn("Field 'dateOfBirth' is missing or invalid for document ID: {}", id);
                                response.setDob(null);
                            }
                            response.setAvatar(content.getString("avatarUrl"));
                            return response;
                        } catch (com.couchbase.client.core.error.DocumentNotFoundException e) {
                            logger.warn("Document not found for ID: {}", id);
                            return null;
                        } catch (Exception e) {
                            logger.error("Failed to fetch document with ID {}: {}", id, e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error executing search query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute search query", e);
        }
    }
}