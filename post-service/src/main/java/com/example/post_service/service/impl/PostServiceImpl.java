package com.example.post_service.service.impl;

import com.example.post_service.dto.request.PostCreatedRequest;
import com.example.post_service.dto.request.PostRequest;
import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.entity.Hashtag;
import com.example.post_service.entity.Post;
import com.example.post_service.entity.PostImage;
import com.example.post_service.entity.SearchPostIndex;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.error.NotExistedErrorCode;

import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.kafka.producer.ProducerNotification;
import com.example.post_service.kafka.producer.ProducerPostEvent;
import com.example.post_service.kafka.producer.ProducerUserActivityEvent;
import com.example.post_service.repository.HashtagRepository;
import com.example.post_service.repository.PostRepository;
import com.example.post_service.repository.redis.IRedisHashtagCache;
import com.example.post_service.service.interfaces.*;

import com.example.post_service.utils.HashtagRedisUpdater;
import com.example.post_service.utils.IndexPostUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements IPostService {

    private final PostRepository postRepository;
    private final ProducerLog logProducer;
    private final ProducerPostEvent producerPostEvent;
    private final ProducerNotification producerNotification;
    private final HashtagRepository hashtagRepository;
    private final ProducerUserActivityEvent userActivityEventProducer;
    private final IPostMapperService postMapperService;
    private final IndexPostUtils indexPostUtils;
    private final IRedisHashtagCache hashtagCacheService;

    public PostServiceImpl(PostRepository postRepository,
                           ProducerPostEvent producerPostEvent,
                           ProducerNotification producerNotification,
                           ProducerLog logProducer,
                           HashtagRepository hashtagRepository,
                           ProducerUserActivityEvent userActivityEventProducer,
                           IPostMapperService postMapperService,
                           IndexPostUtils indexPostUtils,
                           IRedisHashtagCache hashtagCacheService) {
        this.postRepository = postRepository;
        this.producerPostEvent = producerPostEvent;
        this.producerNotification = producerNotification;
        this.logProducer = logProducer;
        this.hashtagRepository = hashtagRepository;
        this.userActivityEventProducer = userActivityEventProducer;
        this.postMapperService = postMapperService;
        this.indexPostUtils = indexPostUtils;
        this.hashtagCacheService = hashtagCacheService;
    }

    @Override
    public List<PostResponse> getPosts(String requestingUserId, String targetUserId, HttpServletRequest httpRequest) {
        try {
            logProducer.sendLog(
                    "post-service",
                    "INFO",
                    "[Post] Getting posts. requestingUserId: " + requestingUserId + ", targetUserId: " + targetUserId);
            List<Post> posts;
            if (targetUserId != null && !targetUserId.equals(requestingUserId)) {
                posts = postRepository.findByUserId(targetUserId).stream()
                        .filter(post -> post.getVisibility() == Post.PostVisibility.PUBLIC
                                || (post.getVisibility() == Post.PostVisibility.FRIENDS_ONLY))
                        .collect(Collectors.toList());
            } else {
                posts = postRepository.findByUserId(requestingUserId);
            }
            return posts.stream()
                    .map(postMapperService::mapToPostResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[Post] Error getting posts. requestingUserId: " + requestingUserId + ", targetUserId: " + targetUserId + ". Error: " + e.getMessage());
            throw e;
        }
    }



    @Override
    public PostResponse getPostById(Long postId, String requestingUserId, HttpServletRequest httpRequest) {
        try {
            logProducer.sendLog("post-service", "INFO", "[Post] Getting post by ID: " + postId + ", requested by user: " + requestingUserId);

            Post post = postRepository.findById(Long.valueOf(postId))
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));

            // Kiểm tra quyền truy cập
            boolean isOwner = post.getUserId().equals(requestingUserId);
            boolean isPublic = post.getVisibility() == Post.PostVisibility.PUBLIC;
            boolean isFriendsOnly = post.getVisibility() == Post.PostVisibility.FRIENDS_ONLY;

            if (!isOwner && !isPublic && isFriendsOnly) {
                // TODO: Gọi user-service kiểm tra nếu 2 người là bạn bè thì cho xem
                throw new AppException(AuthErrorCode.ACCESS_NOT_VIEW_POST);
            }

            return postMapperService.mapToPostResponse(post);
        } catch (Exception e) {
            logProducer.sendLog("post-service", "ERROR", "[Post] Error getting post by ID: " + postId + ", user: " + requestingUserId + ". Error: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public PostResponse createPost(String userId, PostRequest request, HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            logProducer.sendLog("post-service", "INFO", "[Post] Creating post for userId: " + userId);
            Post post = new Post();
            post.setUserId(userId);
            post.setVoiceUrl(request.getVoiceUrl());
            post.setVoiceDuration(request.getVoiceDuration());
            post.setVisibility(request.getVisibility() != null ? request.getVisibility() : Post.PostVisibility.PUBLIC);
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            // Lưu nhiều ảnh
            if (request.getImageUrls() != null) {
                List<PostImage> images = request.getImageUrls().stream()
                        .map(url -> {
                            PostImage image = new PostImage();
                            image.setImageUrl(url);
                            image.setPost(post);
                            return image;
                        }).toList();
                post.setImages(images);
            }
            // Lưu hashtag
            if (request.getHashtags() != null) {
                Set<Hashtag> hashtags = request.getHashtags().stream()
                        .map(name -> hashtagRepository.findByName(name)
                                .orElseGet(() -> {
                                    Hashtag tag = new Hashtag();
                                    tag.setName(name);
                                    return tag;
                                })
                        ).collect(Collectors.toSet());
                post.setHashtags(hashtags);
                // Cập nhật Redis sau khi DB commit
                HashtagRedisUpdater.updateAfterCommit(
                        new HashSet<>(request.getHashtags()), hashtagCacheService
                );
            }
            Post savedPost = postRepository.save(post);
            PostCreatedRequest event = new PostCreatedRequest(userId, savedPost.getPostId(), savedPost.getCreatedAt());
            userActivityEventProducer.sendPostCreatedEvent(event);
            producerPostEvent.publishPostCreatedEvent(savedPost, userId, httpRequest);
            producerNotification.sendNotification("CREATE_POST", userId, post.getUserId(), post.getPostId(), token);
            logProducer.sendLog(
                    "post-service",
                    "INFO",
                    "[Post] Created post successfully for userId: " + userId);
            // Sau khi post đã được lưu vào DB
            try {
                List<String> hashtagNames = savedPost.getHashtags() != null
                        ? savedPost.getHashtags().stream().map(Hashtag::getName).collect(Collectors.toList())
                        : new ArrayList<>();
                SearchPostIndex index = SearchPostIndex.builder()
                        .postId(savedPost.getPostId())
                        .userId(savedPost.getUserId())
                        .title(savedPost.getTitle())
                        .content(savedPost.getContent())
                        .hashtags(hashtagNames)
                        .visibility(savedPost.getVisibility().toString())
                        .createdAt(savedPost.getCreatedAt())
                        .build();
                indexPostUtils.indexPostsBulk(List.of(index));
                logProducer.sendLog("post-service", "DEBUG", "[Post] Successfully bulk index post in Elasticsearch");
            } catch (Exception e) {
                logProducer.sendLog("post-service", "ERROR", "[Post] Failed to bulk index post in Elasticsearch: " + e.getMessage());
            }
            return postMapperService.mapToPostResponse(savedPost);
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[Post] Error creating post for userId: " + userId + ". Error: " + e);
            throw new AppException(AuthErrorCode.FAILED_CREATE_POST);
        }
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, String userId, PostRequest request, HttpServletRequest httpRequest) {
        try {
            logProducer.sendLog("post-service", "INFO", "[Post] Updating postId: " + postId + " by userId: " + userId);
            // Lấy post hiện tại
            Post oldPost = postRepository.findById(postId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));

            // Kiểm tra quyền sửa bài
            if (!oldPost.getUserId().equals(userId)) {
                throw new AppException(AuthErrorCode.NO_PERMISSION_EDIT_POST);
            }

            // Sao lưu dữ liệu cũ để log/event
            Post oldPostCopy = new Post();
            oldPostCopy.setPostId(oldPost.getPostId());
            oldPostCopy.setUserId(oldPost.getUserId());
            oldPostCopy.setTitle(oldPost.getTitle());
            oldPostCopy.setContent(oldPost.getContent());
            oldPostCopy.setVoiceUrl(oldPost.getVoiceUrl());
            oldPostCopy.setVoiceDuration(oldPost.getVoiceDuration());
            oldPostCopy.setImages(new ArrayList<>(oldPost.getImages()));
            oldPostCopy.setHashtags(new HashSet<>(oldPost.getHashtags()));
            oldPostCopy.setVisibility(oldPost.getVisibility());

            if (request.getVoiceUrl() != null) {
                oldPost.setVoiceUrl(request.getVoiceUrl());
            }
            if (request.getVoiceDuration() != null) {
                oldPost.setVoiceDuration(request.getVoiceDuration());
            }
            if (request.getVisibility() != null) {
                oldPost.setVisibility(request.getVisibility());
            }
            if(request.getTitle() != null) {
                oldPost.setTitle(request.getTitle());
            }
            if(request.getContent() != null) {
                oldPost.setContent(request.getContent());
            }
            // Cập nhật danh sách ảnh nếu có
            if (request.getImageUrls() != null) {
                oldPost.getImages().clear();
                List<PostImage> images = request.getImageUrls().stream()
                        .map(url -> {
                            PostImage image = new PostImage();
                            image.setImageUrl(url);
                            image.setPost(oldPost);
                            return image;
                        })
                        .toList();
                oldPost.getImages().addAll(images);
            }
            // 2. Lưu hashtag
            if (request.getHashtags() != null) {
                Set<Hashtag> hashtags = request.getHashtags().stream()
                        .map(name -> hashtagRepository.findByName(name)
                                .orElseGet(() -> {
                                    Hashtag tag = new Hashtag();
                                    tag.setName(name);
                                    return tag;
                                })
                        ).collect(Collectors.toSet());
                oldPost.setHashtags(hashtags);

                // Cập nhật Redis sau khi DB commit
                HashtagRedisUpdater.updateAfterCommit(
                        new HashSet<>(request.getHashtags()), hashtagCacheService
                );
            }
            // Lưu post đã cập nhật
            Post updatedPost = postRepository.save(oldPost);
            // Gửi event cập nhật
            producerPostEvent.publishPostUpdatedEvent(updatedPost, oldPostCopy, userId, httpRequest);
            logProducer.sendLog(
                    "post-service",
                    "DEBUG",
                    "[Update Post] Updated post successfully for userId: " + userId);

            // Sau khi post đã được lưu vào DB
            try {
                List<String> hashtagNames = updatedPost.getHashtags() != null
                        ? updatedPost.getHashtags().stream().map(Hashtag::getName).collect(Collectors.toList())
                        : new ArrayList<>();
                SearchPostIndex index = SearchPostIndex.builder()
                        .postId(updatedPost.getPostId())
                        .userId(updatedPost.getUserId())
                        .title(updatedPost.getTitle())
                        .content(updatedPost.getContent())
                        .hashtags(hashtagNames)
                        .visibility(updatedPost.getVisibility().toString())
                        .createdAt(updatedPost.getCreatedAt())
                        .build();
                indexPostUtils.indexPostsBulk(List.of(index));
                logProducer.sendLog("post-service", "DEBUG", "[Update Post] Successfully bulk index post in Elasticsearch");
            } catch (Exception e) {
                logProducer.sendLog("post-service", "ERROR", "[Update Post] Failed to bulk index post in Elasticsearch: " + e.getMessage());
            }

            return postMapperService.mapToPostResponse(updatedPost);
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[Update Post] Error updating postId: " + postId + ". Error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_POST);
        }
    }

    @Override
    @Transactional
    public void deletePost(Long postId, String userId, HttpServletRequest httpRequest) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            if (!post.getUserId().equals(userId)) {
                throw new AppException(AuthErrorCode.NO_PERMISSION_DELETE_POST);
            }
            producerPostEvent.publishPostDeletedEvent(post, userId, httpRequest);
            postRepository.delete(post);
            logProducer.sendLog(
                    "post-service",
                    "DEBUG",
                    "[Post] Deleted post successfully for userId: " + userId);
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[Post] Error deleting postId: " + postId + ". Error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_DELETE_POST);
        }
    }
}