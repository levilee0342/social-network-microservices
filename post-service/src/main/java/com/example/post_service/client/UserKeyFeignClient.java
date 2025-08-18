package com.example.post_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "identity-service")
@Service
public interface UserKeyFeignClient {

    @GetMapping("/user/keys/public/{userId}")
    String getPublicKey(@PathVariable("userId") String userId);

    @PostMapping("/user/keys/private")
    String getPrivateKey(@RequestHeader("Authorization") String token);
}
