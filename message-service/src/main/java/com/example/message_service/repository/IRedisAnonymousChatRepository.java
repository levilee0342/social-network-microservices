package com.example.message_service.repository;

import com.example.message_service.dto.request.AnonymousChatRequest;

import java.util.List;
import java.util.Optional;

public interface IRedisAnonymousChatRepository {
    void save(AnonymousChatRequest request);
    Optional<AnonymousChatRequest> findById(String userId);
    List<AnonymousChatRequest> findPotentialMatches(AnonymousChatRequest currentRequest, int limit);
}
