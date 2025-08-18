package com.example.search_service.service.interfaces;

import com.example.search_service.entity.PostDocument;

public interface IPostDocumentRepository {
    void save(PostDocument post);
    void delete(String postId);
}
