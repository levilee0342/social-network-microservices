package com.example.post_service.repository;

import com.example.post_service.entity.UserTypePreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTypePreferenceRepository extends JpaRepository<UserTypePreference, Long> {
    List<UserTypePreference> findByUserId(String userId);
    void deleteByUserId(String userId);
}
