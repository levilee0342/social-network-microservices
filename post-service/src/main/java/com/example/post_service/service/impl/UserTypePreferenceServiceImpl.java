package com.example.post_service.service.impl;

import com.example.post_service.dto.request.UserTypePreferenceRequest;
import com.example.post_service.entity.TypeContent;
import com.example.post_service.entity.UserTypePreference;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.repository.TypeContentRepository;
import com.example.post_service.repository.UserTypePreferenceRepository;
import com.example.post_service.service.interfaces.IUserTypePreferenceService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserTypePreferenceServiceImpl implements IUserTypePreferenceService {

    private final UserTypePreferenceRepository preferenceRepository;
    private final TypeContentRepository typeContentRepository;
    private final ProducerLog logProducer;

    public UserTypePreferenceServiceImpl(UserTypePreferenceRepository preferenceRepository,
                                         TypeContentRepository typeContentRepository,
                                         ProducerLog logProducer) {
        this.preferenceRepository = preferenceRepository;
        this.typeContentRepository = typeContentRepository;
        this.logProducer = logProducer;
    }

    @Override
    public void saveUserPreferences(String userId, UserTypePreferenceRequest request){
        try {
            preferenceRepository.deleteByUserId(userId);
            // Lưu mới
            for (Long typeId : request.getTypeIds()) {
                Optional<TypeContent> optionalType = typeContentRepository.findById(typeId);
                if (optionalType.isPresent()) {
                    UserTypePreference preference = new UserTypePreference();
                    preference.setUserId(userId);
                    preference.setTypeContent(optionalType.get());
                    preferenceRepository.save(preference);
                }
            }
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[User Type Preference] Error save type preference for user. Error: " + e.getMessage());
        }
    }
}
