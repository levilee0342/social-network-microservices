package com.example.post_service.service.impl;

import com.example.post_service.dto.request.UserInteractionEventRequest;
import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.entity.Post;
import com.example.post_service.entity.PostShare;
import com.example.post_service.entity.TypeContent;
import com.example.post_service.error.InvalidErrorCode;
import com.example.post_service.error.NotExistedErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.kafka.producer.ProducerNotification;
import com.example.post_service.kafka.producer.ProducerUserPreference;
import com.example.post_service.repository.PostRepository;
import com.example.post_service.repository.PostShareRepository;
import com.example.post_service.service.interfaces.IPostMapperService;
import com.example.post_service.service.interfaces.IPostShareService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostShareServiceImpl implements IPostShareService {

    private final PostRepository postRepository;
    private final PostShareRepository postShareRepository;
    private final IPostMapperService postMapperService;
    private final ProducerNotification producerNotification;
    private final ProducerUserPreference producerUserPreference;
    private final ProducerLog logProducer;

    public PostShareServiceImpl(PostRepository postRepository,
                                PostShareRepository postShareRepository,
                                IPostMapperService postMapperService,
                                ProducerNotification producerNotification,
                                ProducerUserPreference producerUserPreference,
                                ProducerLog logProducer) {
        this.postRepository = postRepository;
        this.postShareRepository = postShareRepository;
        this.postMapperService = postMapperService;
        this.producerNotification = producerNotification;
        this.producerUserPreference = producerUserPreference;
        this.logProducer = logProducer;
    }

    @Override
    public List<PostResponse> getSharedPostsByUser(String userId, HttpServletRequest httpRequest) {
        try {
            List<PostShare> shares = postShareRepository.findByUserId(userId);
            return shares.stream()
                    .map(share -> postMapperService.mapToPostShareResponse(
                            share.getPost(),
                            share.getShareId(),
                            share.getSharedAt()
                    ))
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
    public void sharePost(String userId, Long postId, String content, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            // Kiểm tra nếu user đã chia sẻ bài viết này
            if (postShareRepository.existsByUserIdAndPost_PostId(userId, postId)) {
                logProducer.sendLog(
                        "post-service",
                        "WARN",
                        "[Post] User " + userId + " has already shared post " + postId);
                throw new AppException(NotExistedErrorCode.POST_ALREADY_SHARED);
            }
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            PostShare share = new PostShare();
            share.setUserId(userId);
            share.setPost(post);
            share.setContent(content);
            share.setSharedAt(System.currentTimeMillis());
            postShareRepository.save(share);
            // Pub sự kiện xử lý điểm tương tác
            ArrayList<Long> typeIds = post.getTypeContent().stream()
                    .map(TypeContent::getTypeId)
                    .collect(Collectors.toCollection(ArrayList::new));

            UserInteractionEventRequest reactionRequest = new UserInteractionEventRequest();
            reactionRequest.setUserId(userId);
            reactionRequest.setEventType("SHARE");
            reactionRequest.setTypeIds(typeIds);
            reactionRequest.setTimestamp(System.currentTimeMillis());

            producerUserPreference.publisherUserPreferenceEvent(reactionRequest);
            System.out.println("[DEBUG] Sending user interaction event: " + reactionRequest);
            producerNotification.sendNotification("NEW_SHARE", userId, post.getUserId(), postId, token);
            logProducer.sendLog(
                    "post-service",
                    "DEBUG",
                    "[Post] Post " + postId + " shared successfully by user " + userId);
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[Post] Failed to share post " + postId + ": " + e.getMessage());
            throw new AppException(InvalidErrorCode.INVALID_FETCH_SHARE_P0ST);
        }
    }
}
