package com.example.user_service.repository;

public interface IRedisOtpGeneratorRepository {
    String generateAndStoreOtp();
}
