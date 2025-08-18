package com.example.notification_service.service.impl;

import com.example.notification_service.dto.response.PublicProfileResponse;
import com.example.notification_service.repository.redis.IRedisPublicProfile;
import com.example.notification_service.service.interfaces.IContentGenerator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostContentGeneratorImpl implements IContentGenerator {
    private final IRedisPublicProfile redisPublicProfile;

    public PostContentGeneratorImpl(IRedisPublicProfile redisPublicProfile) {
        this.redisPublicProfile = redisPublicProfile;
    }

    @Override
    public String generateGroupedContent(String eventType, List<String> actorIds) {
        if (actorIds == null || actorIds.isEmpty()) return "Bạn có tương tác mới.";
        // Đảo ngược actorIds để lấy người tương tác mới nhất
        List<String> reversedIds = actorIds.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    Collections.reverse(list);
                    return list;
                }));
        // Lấy profile theo thứ tự đảo ngược
        List<PublicProfileResponse> profiles = reversedIds.stream()
                .map(redisPublicProfile::getPublicProfileByUserId)
                .filter(p -> p != null)
                .toList();
        // Lấy tên 2 người mới nhất
        List<String> names = profiles.stream()
                .limit(2)
                .map(PublicProfileResponse::getFullName)
                .toList();
        int more = profiles.size() - names.size();
        return switch (eventType) {
            case "NEW_LIKE" -> more > 0
                    ? String.format("%s và %d người khác đã thích bài viết của bạn", String.join(", ", names), more)
                    : String.format("%s đã thích bài viết của bạn", String.join(", ", names));
            case "NEW_LIKE_COMMENT" -> more > 0
                    ? String.format("%s và %d người khác đã thích bình luận của bạn", String.join(", ", names), more)
                    : String.format("%s đã thích thích bình luận của bạn", String.join(", ", names));
            case "NEW_COMMENT" -> more > 0
                    ? String.format("%s và %d người khác đã bình luận bài viết của bạn", String.join(", ", names), more)
                    : String.format("%s đã bình luận bài viết của bạn", String.join(", ", names));
            case "NEW_REPLY_COMMENT" -> more > 0
                    ? String.format("%s và %d người khác đã trả lời bình luận của bạn", String.join(", ", names), more)
                    : String.format("%s đã trả lời bình luận của bạn", String.join(", ", names));
            case "NEW_SHARE" -> more > 0
                    ? String.format("%s và %d người khác đã chia sẻ bài viết của bạn", String.join(", ", names), more)
                    : String.format("%s đã chia sẻ bài viết của bạn", String.join(", ", names));
            default -> "Bạn có tương tác mới.";
        };
    }
}
