package com.example.post_service.service.impl;

import com.example.post_service.dto.request.UserInteractionEventRequest;
import com.example.post_service.dto.response.PublicProfileResponse;
import com.example.post_service.entity.Like;
import com.example.post_service.entity.Post;
import com.example.post_service.entity.TypeContent;
import com.example.post_service.enums.ReactionType;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.error.InvalidErrorCode;
import com.example.post_service.error.NotExistedErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.kafka.producer.ProducerNotification;
import com.example.post_service.kafka.producer.ProducerPostEvent;
import com.example.post_service.kafka.producer.ProducerUserPreference;
import com.example.post_service.repository.ReactionRepository;
import com.example.post_service.repository.PostRepository;
import com.example.post_service.repository.redis.IRedisPublicProfile;
import com.example.post_service.service.interfaces.IReactionService;
import com.example.post_service.enums.PostEventType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReactionServiceImpl implements IReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final ProducerNotification producerNotification;
    private final ProducerPostEvent producerEvent;
    private final ProducerUserPreference producerUserPreference;
    private final IRedisPublicProfile redisPublicProfile;
    private final ProducerLog producerLog;

    public ReactionServiceImpl(ReactionRepository reactionRepository,
                               PostRepository postRepository,
                               ProducerNotification producerNotification,
                               ProducerPostEvent producerEvent,
                               ProducerUserPreference producerUserPreference,
                               IRedisPublicProfile redisPublicProfile,
                               ProducerLog producerLog) {
        this.reactionRepository = reactionRepository;
        this.postRepository = postRepository;
        this.producerNotification = producerNotification;
        this.producerEvent = producerEvent;
        this.producerUserPreference = producerUserPreference;
        this.redisPublicProfile = redisPublicProfile;
        this.producerLog = producerLog;
    }

    @Override
    @Transactional
    public void reactToPost(Long postId, ReactionType type, String userId, HttpServletRequest request) {
        try {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            Optional<Like> existingReaction = reactionRepository.findByPost_PostIdAndUserId(postId, userId);
            if (existingReaction.isPresent()) {
                Like existing = existingReaction.get();
                if (existing.getType() == type) {
                    throw new AppException(InvalidErrorCode.YOU_LIKED_POST);
                }
                existing.setType(type);
                reactionRepository.save(existing);
            } else {
                Like reaction = new Like();
                reaction.setPost(post);
                reaction.setUserId(userId);
                reaction.setType(type);
                reactionRepository.save(reaction);
            }

            // Lưu sự kiện post
            producerEvent.publishLikeEvent(postId, userId, PostEventType.POST_LIKED, request);

            // Pub sự kiện xử lý điểm tương tác
            ArrayList<Long> typeIds = post.getTypeContent().stream()
                    .map(TypeContent::getTypeId)
                    .collect(Collectors.toCollection(ArrayList::new));

            UserInteractionEventRequest reactionRequest = new UserInteractionEventRequest();
            reactionRequest.setUserId(userId);
            reactionRequest.setEventType("LIKE");
            reactionRequest.setTypeIds(typeIds);
            reactionRequest.setTimestamp(System.currentTimeMillis());

            producerUserPreference.publisherUserPreferenceEvent(reactionRequest);
            System.out.println("[DEBUG] Sending user interaction event: " + reactionRequest);

            //Pub sự kiện thông báo
            producerNotification.sendNotification("NEW_LIKE", userId, post.getUserId(), postId, token);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "[Reaction] Failed to reaction to post. Reason: " + e
            );
        }
    }

    @Override
    @Transactional
    public void unreactToPost(Long postId, ReactionType type, String userId, HttpServletRequest request) {
        try {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            Optional<Like> existingReaction = reactionRepository.findByPost_PostIdAndUserId(postId, userId);
            if (existingReaction.isPresent()) {
                Like existing = existingReaction.get();
                reactionRepository.delete(existing);
            }
            producerEvent.publishLikeEvent(postId, userId, PostEventType.POST_UNLIKED, request);
            producerNotification.sendNotification("UN_LIKE", userId, post.getUserId(), postId, token);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "[Reaction] Failed to delete reaction to post. Reason: " + e
            );
        }
    }

    @Override
    public long countReactionsByPostId(Long postId) {
        return reactionRepository.countByPost_PostId(postId);
    }

    @Override
    public Map<String, Object> getReactionDetails(Long postId) {
        try {
            // Đếm số lượng theo từng loại reaction
            List<Object[]> countResult = reactionRepository.countReactionsGroupByType(postId);
            Map<String, Integer> countMap = new HashMap<>();

            // Khởi tạo tất cả các loại reaction với giá trị 0
            for (ReactionType type : EnumSet.allOf(ReactionType.class)) {
                countMap.put(type.name(), 0);
            }

            // Cập nhật lại số lượng theo dữ liệu thật
            for (Object[] row : countResult) {
                String type = row[0].toString();
                Long count = (Long) row[1];
                countMap.put(type, count.intValue());
            }

            // Lấy danh sách userId và type của họ (không lọc theo type)
            List<Object[]> userResult = reactionRepository.findUserIdsByPostId(postId);

            // Map dữ liệu người dùng và reaction type
            List<Map<String, String>> users = userResult.stream()
                    .map(row -> {
                        String userId = row[0].toString();
                        String reactionType = row[1].toString();

                        PublicProfileResponse profile = redisPublicProfile.getPublicProfileByUserId(userId);

                        Map<String, String> map = new HashMap<>();
                        map.put("fullName", profile.getFullName());
                        map.put("avatarUrl", profile.getAvatarUrl());
                        map.put("reactionType", reactionType);
                        return map;
                    })
                    .collect(Collectors.toList());

            // Kết quả trả về
            Map<String, Object> response = new HashMap<>();
            response.put("counts", countMap);  // Đếm số lượng theo từng loại
            response.put("users", users);      // Danh sách user + reactionType

            return response;

        } catch (Exception e) {
            producerLog.sendLog("post-service", "ERROR", "[Reaction] Failed to get reaction details. Reason: " + e);
            throw new AppException(AuthErrorCode.FAILED_COUNT_REACTION);
        }
    }

}