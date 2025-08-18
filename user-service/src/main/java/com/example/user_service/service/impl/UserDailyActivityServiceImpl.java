package com.example.user_service.service.impl;

import com.example.user_service.entity.UserDailyActivity;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseUserDailyActivityRepository;
import com.example.user_service.service.interfaces.IUserDailyActivityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class UserDailyActivityServiceImpl implements IUserDailyActivityService {

    private final ICouchbaseUserDailyActivityRepository activityRepository;
    private final ProducerLog logProducer;

    public UserDailyActivityServiceImpl(ICouchbaseUserDailyActivityRepository activityRepository,
                                        ProducerLog logProducer) {
        this.activityRepository = activityRepository;
        this.logProducer = logProducer;
    }

    @Override
    @Transactional
    public void recordActivity(String userId) {
        try {
            Long todayEpoch = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

            UserDailyActivity todayActivity = activityRepository.findByUserIdAndDate(userId, todayEpoch)
                    .orElseGet(() -> {
                        UserDailyActivity activity = new UserDailyActivity();
                        activity.setUserId(userId);
                        activity.setDate(todayEpoch);
                        activity.setCount(0);
                        activity.setStreak(0);
                        return activity;
                    });
            todayActivity.setCount(todayActivity.getCount() + 1);
            if (todayActivity.getCount() == 1) {
                Long yesterdayEpoch = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
                List<UserDailyActivity> activities = activityRepository.findByUserIdOrderByDateDesc(userId);

                if (!activities.isEmpty() && activities.get(0).getDate().equals(yesterdayEpoch)) {
                    todayActivity.setStreak(activities.get(0).getStreak() + 1);
                } else {
                    todayActivity.setStreak(1);
                }
            }
            activityRepository.save(todayActivity);
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[UserActivity] Failed to record activity for userId: " + userId + " | Error: " + e.getMessage());
            throw e; // Re-throw to preserve original transactional and error handling behavior
        }
    }
}
