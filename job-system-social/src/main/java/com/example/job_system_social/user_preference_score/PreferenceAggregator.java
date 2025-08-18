package com.example.job_system_social.user_preference_score;

import com.example.job_system_social.model.UserInteractionEventRequest;
import com.example.job_system_social.model.UserPreferenceScoreRequest;
import org.apache.flink.api.common.functions.AggregateFunction;

public class PreferenceAggregator implements AggregateFunction<UserInteractionEventRequest, PreferenceAggregator.PreferenceAccumulator, UserPreferenceScoreRequest> {

    public static class PreferenceAccumulator {
        public String userId;
        public Long typeId;
        public int score = 0;
    }

    @Override
    public PreferenceAccumulator createAccumulator() {
        return new PreferenceAccumulator();
    }

    @Override
    public PreferenceAccumulator add(UserInteractionEventRequest value, PreferenceAccumulator acc) {
        acc.userId = value.getUserId();
        acc.typeId = value.getTypeId();
        int weight = switch (value.getEventType()) {
            case "LIKE" -> 1;
            case "COMMENT" -> 2;
            case "SHARE" -> 3;
            default -> 0;
        };
        acc.score += weight;
        System.out.printf("[DEBUG] Adding %s event for user %s, type %d, weight %d, new score %d%n",
                value.getEventType(), value.getUserId(), value.getTypeId(), weight, acc.score);
        return acc;
    }

    @Override
    public UserPreferenceScoreRequest getResult(PreferenceAccumulator acc) {
        System.out.printf("[DEBUG] Final score for user %s, type %d: %d%n",
                acc.userId, acc.typeId, acc.score);
        return new UserPreferenceScoreRequest(acc.userId, acc.typeId, acc.score);
    }

    @Override
    public PreferenceAccumulator merge(PreferenceAccumulator a, PreferenceAccumulator b) {
        a.score += b.score;
        return a;
    }
}