package com.example.search_service.service.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Collection;
import com.example.search_service.entity.PostDocument;
import com.example.search_service.service.interfaces.IPostDocumentRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class PostDocumentRepositoryImpl implements IPostDocumentRepository {

    private final Collection collection;

    public PostDocumentRepositoryImpl(@Qualifier("searchBucket") Bucket bucket) {
        this.collection = bucket.scope("search_post_result").collection("post");
    }

    @Override
    public void save(PostDocument post) {
        collection.insert(post.getPostId(), post);
    }

    @Override
    public void delete(String postId) {
        collection.remove(postId);
    }
}
