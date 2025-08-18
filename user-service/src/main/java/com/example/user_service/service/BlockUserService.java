package com.example.user_service.service;

import com.example.user_service.repository.ICouchbaseBlockUserRepository;
import org.springframework.stereotype.Service;

@Service
public class BlockUserService {

    private final ICouchbaseBlockUserRepository blockUser;

    public BlockUserService(ICouchbaseBlockUserRepository blockUser) {
        this.blockUser = blockUser;
    }

    public void blockUser(String blockerId, String blockedId){
        blockUser.blockUser(blockerId, blockedId);
    }

    public void unblockUser(String blockerId, String blockedId){
        blockUser.unblockUser(blockerId, blockedId);
    }

    public boolean isBlocked(String user1, String user2){
        return blockUser.isBlocked(user1, user2);
    }
}
