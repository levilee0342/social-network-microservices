package com.example.user_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.kv.UpsertOptions;
import com.example.user_service.entity.OtpRecord;
import com.example.user_service.error.InvalidErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseOtpRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class CouchbaseOtpRepository implements ICouchbaseOtpRepository {
    private final Collection otpCollection;
    private final ProducerLog logProducer;

    public CouchbaseOtpRepository(@Qualifier("userBucket") Bucket userBucket,
                                  ProducerLog logProducer) {
        this.otpCollection = userBucket.scope("identity").collection("otp");
        this.logProducer = logProducer;
    }

    @Override
    public void saveOtp(String otpKey, OtpRecord otpRecord) {
        try {
            otpCollection.upsert(
                    otpKey,
                    otpRecord,
                    UpsertOptions.upsertOptions().expiry(Duration.ofMinutes(5))
            );
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[OTP] Successfully saved OTP for key: " + otpKey);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[OTP] Failed to save OTP for key: " + otpKey + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public OtpRecord getOtp(String otpKey) {
        try {
            OtpRecord otpRecord = otpCollection.get(otpKey).contentAs(OtpRecord.class);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[OTP] Successfully retrieved OTP for key: " + otpKey);
            return otpRecord;
        } catch (com.couchbase.client.core.error.DocumentNotFoundException e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[OTP] OTP not found for key: " + otpKey);
            return null;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[OTP] Failed to retrieve OTP for key: " + otpKey + ". Reason: " + e.getMessage());
            throw new AppException(InvalidErrorCode.INVALID_OTP);
        }
    }

    @Override
    public void removeOtp(String otpKey) {
        try {
            otpCollection.remove(otpKey);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[OTP] Successfully removed OTP for key: " + otpKey);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[OTP] Failed to remove OTP for key: " + otpKey + ". Reason: " + e.getMessage());
            throw e;
        }
    }
}