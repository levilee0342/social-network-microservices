package com.example.user_service.entity;

public class OtpRecord {
    private String email;
    private String otp;
    private long expiryTime;

    public OtpRecord() {}

    public OtpRecord(String email, String otp, long expiryTime) {
        this.email = email;
        this.otp = otp;
        this.expiryTime = expiryTime;
    }

    public String getEmail() {
        return email;
    }

    public String getOtp() {
        return otp;
    }

    public long getExpiryTime() {
        return expiryTime;
    }
}
