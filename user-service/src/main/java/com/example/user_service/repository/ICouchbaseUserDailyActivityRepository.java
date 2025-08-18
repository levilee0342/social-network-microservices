package com.example.user_service.repository;

import com.example.user_service.entity.UserDailyActivity;

import java.util.List;
import java.util.Optional;

public interface ICouchbaseUserDailyActivityRepository {
    Optional<UserDailyActivity> findByUserIdAndDate(String userId, Long date);
    List<UserDailyActivity> findByUserIdOrderByDateDesc(String userId);
    UserDailyActivity save(UserDailyActivity activity);
}
