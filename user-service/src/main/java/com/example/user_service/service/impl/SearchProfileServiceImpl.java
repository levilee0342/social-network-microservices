package com.example.user_service.service.impl;

import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.dto.response.PublicProfileResponse;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseProfileRepository;
import com.example.user_service.repository.ICouchbaseSearchProfileRepository;
import com.example.user_service.repository.IRedisProfileManagerRepository;
import com.example.user_service.service.interfaces.IProfileMapperService;
import com.example.user_service.service.interfaces.ISearchProfileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchProfileServiceImpl implements ISearchProfileService {

    private final ICouchbaseProfileRepository profileManagerRepository;
    private final ICouchbaseSearchProfileRepository searchProfileRepository;
    private final IProfileMapperService profileMapper;
    private final IRedisProfileManagerRepository cacheManager;
    private final ProducerLog logProducer;

    public SearchProfileServiceImpl(ICouchbaseProfileRepository profileManagerRepository,
                                    ICouchbaseSearchProfileRepository searchProfileRepository,
                                    IRedisProfileManagerRepository cacheManager,
                                    IProfileMapperService profileMapper,
                                    ProducerLog logProducer) {
        this.profileManagerRepository = profileManagerRepository;
        this.searchProfileRepository = searchProfileRepository;
        this.cacheManager = cacheManager;
        this.profileMapper = profileMapper;
        this.logProducer = logProducer;
    }

    @Override
    public List<ProfileResponse> searchProfiles(String fullName, String address, String phone) {
        logProducer.sendLog(
                "user-service",
                "INFO",
                "[Profile] Searching profiles");
        try {
            List<ProfileResponse> profiles = searchProfileRepository.search(fullName, address, phone);
            List<ProfileResponse> responses = profiles.stream()
                    .map(profileMapper::mapToResponse)
                    .toList();
            for (ProfileResponse response : responses) {
                cacheManager.cacheProfile(response.getUserId(), response);
                if (response.getIsPublic()) {
                    PublicProfileResponse publicResponse = profileMapper.mapToPublicResponse(profileManagerRepository.getProfile(response.getUserId()));
                    cacheManager.cachePublicProfile(response.getUserId(), publicResponse);
                }
            }
            return responses;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Profile search failed. Reason: " + e.getMessage());
            throw e;
        }
    }
}
