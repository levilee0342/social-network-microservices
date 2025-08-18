package com.example.user_service.repository;

import com.example.user_service.entity.OtpRecord;

public interface ICouchbaseOtpRepository {
    void saveOtp(String otpKey, OtpRecord otpRecord);
    OtpRecord getOtp(String otpKey);
    void removeOtp(String otpKey);
}
