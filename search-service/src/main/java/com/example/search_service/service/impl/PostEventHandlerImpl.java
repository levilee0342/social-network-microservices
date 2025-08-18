package com.example.search_service.service.impl;

import com.example.search_service.dto.request.PostEvent;
import com.example.search_service.entity.PostDocument;
import com.example.search_service.service.interfaces.IPostDocumentRepository;
import com.example.search_service.service.interfaces.IPostEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PostEventHandlerImpl implements IPostEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(PostEventHandlerImpl.class);
    private final IPostDocumentRepository postDocumentRepository;

    public PostEventHandlerImpl(IPostDocumentRepository postDocumentRepository) {
        this.postDocumentRepository = postDocumentRepository;
    }

    @Override
    public void handle(PostEvent event) {
        PostDocument post = new PostDocument();
        post.setPostId(event.getPostId());
        post.setContent(event.getContent());
        post.setUserId(event.getUserId());
        post.setHashtags(event.getHashtags());
        post.setVisibility(event.getVisibility());
        post.setCreatedAt(event.getCreatedAt());
        post.setType("post");

        switch (event.getType()) {
            case "CREATE":
            case "UPDATE":
                postDocumentRepository.save(post);
                logger.info("Saved PostDocument: {}", post.getPostId());
                break;
            case "DELETE":
                postDocumentRepository.delete(event.getPostId());
                logger.info("Deleted PostDocument: {}", post.getPostId());
                break;
            default:
                logger.warn("Unknown event type: {}", event.getType());
        }
    }
}
