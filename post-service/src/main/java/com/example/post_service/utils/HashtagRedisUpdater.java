package com.example.post_service.utils;

import com.example.post_service.repository.redis.IRedisHashtagCache;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Set;

@Component
public class HashtagRedisUpdater {
    public static void updateAfterCommit(Set<String> hashtagNames, IRedisHashtagCache cacheService) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    for (String name : hashtagNames) {
                        cacheService.updateHashtagCache(name, true);
                    }
                }
            });
        }
    }
}
