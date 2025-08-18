package com.example.post_service.service.impl;

import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.entity.Post;
import com.example.post_service.entity.PostBookmark;
import com.example.post_service.error.InvalidErrorCode;
import com.example.post_service.error.NotExistedErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.repository.PostBookmarkRepository;
import com.example.post_service.repository.PostRepository;
import com.example.post_service.service.interfaces.IPostBookmarkService;
import com.example.post_service.service.interfaces.IPostMapperService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostBookmarkServiceImpl implements IPostBookmarkService {

    private final PostRepository postRepository;
    private final PostBookmarkRepository postBookmarkRepository;
    private final IPostMapperService postMapperService;
    private final ProducerLog logProducer;

    public PostBookmarkServiceImpl(PostRepository postRepository,
                                PostBookmarkRepository postBookmarkRepository,
                                IPostMapperService postMapperService,
                                ProducerLog logProducer){
        this.postRepository = postRepository;
        this.postBookmarkRepository = postBookmarkRepository;
        this.postMapperService = postMapperService;
        this.logProducer = logProducer;
    }

    @Override
    public List<PostResponse> getBookmarkPostsByUser(String userId, HttpServletRequest httpRequest) {
        try {
            List<PostBookmark> bookmarks = postBookmarkRepository.findByUserId(userId);
            return bookmarks.stream()
                    .map(PostBookmark::getPost)
                    .map(postMapperService::mapToPostResponse)
                    .toList();
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[Post] Failed to fetch shared posts for userId: " + userId + ": " + e);
            throw new AppException(InvalidErrorCode.INVALID_FETCH_SHARE_P0ST);
        }
    }

    @Override
    public void BookmarkPost(String userId, Long postId, HttpServletRequest httpRequest) {
        try {
            // Kiểm tra nếu user đã chia sẻ bài viết này
            if (postBookmarkRepository.existsByUserIdAndPost_PostId(userId, postId)) {
                logProducer.sendLog("post-service", "WARN", "[Post] User " + userId + " has already shared post " + postId);
                throw new AppException(NotExistedErrorCode.POST_ALREADY_SHARED);
            }
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            PostBookmark bookmark = new PostBookmark();
            bookmark.setUserId(userId);
            bookmark.setPost(post);
            bookmark.setSavedAt(System.currentTimeMillis());
            postBookmarkRepository.save(bookmark);
            logProducer.sendLog(
                    "post-service",
                    "DEBUG",
                    "[Post] Post " + postId + " shared successfully by user " + userId);
        } catch (AppException e) {
            throw new AppException(InvalidErrorCode.INVALID_FETCH_SHARE_P0ST);
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[Post] Failed to share post " + postId + ": " + e.getMessage());
            throw new AppException(InvalidErrorCode.INVALID_FETCH_SHARE_P0ST);
        }
    }
}
