package com.example.user_service.repository.impl;

import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.IRedisOtpGeneratorRepository;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisOtpGeneratorRepository implements IRedisOtpGeneratorRepository {
    private final JedisPool jedisPool;
    private final ProducerLog logProducer;
    private static final String OTP_QUEUE_KEY = "otp:queue";
    private static final String OTP_SET_KEY = "otp:used";

    public RedisOtpGeneratorRepository(JedisPool jedisPool, ProducerLog logProducer) {
        this.jedisPool = jedisPool;
        this.logProducer = logProducer;
    }

    @Override
    public String generateAndStoreOtp() {
        logProducer.sendLog("user-service", "INFO", "[OTP] Generating and storing OTP");
        try (Jedis jedis = jedisPool.getResource()) {
            String otp = jedis.rpop(OTP_QUEUE_KEY);
            if (otp == null) {
                logProducer.sendLog("user-service", "ERROR", "[OTP] No OTP available in queue");
                throw new AppException(AuthErrorCode.FAIL_GENERATE_OTP);
            }
            jedis.srem(OTP_SET_KEY, otp);
            logProducer.sendLog("user-service", "INFO", "[OTP] Successfully generated and stored OTP: " + otp);
            return otp;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[OTP] Failed to generate and store OTP. Reason: " + e.getMessage());
            throw e;
        }
    }
}