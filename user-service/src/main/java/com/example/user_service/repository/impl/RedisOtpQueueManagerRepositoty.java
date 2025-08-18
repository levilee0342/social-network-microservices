package com.example.user_service.repository.impl;

import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.IRedisOtpQueueManagerRepositoty;
import com.example.user_service.scheduler.BackupOtpScheduler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RedisOtpQueueManagerRepositoty implements IRedisOtpQueueManagerRepositoty {

    private final JedisPool jedisPool;
    private final ScheduledExecutorService scheduler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BackupOtpScheduler backupOtpScheduler;
    private final ProducerLog logProducer;

    private static final Path OTP_BACKUP_PATH = Paths.get("backup/otp_backup.json");
    private static final int OTP_LENGTH = 6;
    private static final int OTP_QUEUE_MIN_SIZE = 1000;
    private static final int OTP_QUEUE_REFILL_SIZE = 10000;
    private static final long OTP_EXPIRY_SECONDS = 300;
    private static final String OTP_QUEUE_KEY = "otp:queue";
    private static final String OTP_SET_KEY = "otp:used";

    public RedisOtpQueueManagerRepositoty(JedisPool jedisPool,
                                          BackupOtpScheduler backupOtpScheduler,
                                          ProducerLog logProducer) {
        this.jedisPool = jedisPool;
        this.backupOtpScheduler = backupOtpScheduler;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.logProducer = logProducer;
        checkRedisPersistence();
    }

    private void checkRedisPersistence() {
        try (Jedis jedis = jedisPool.getResource()) {
            String aofEnabled = jedis.configGet("appendonly").get("appendonly");
            if (!"yes".equals(aofEnabled)) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[REDIS] Redis AOF is not enabled. Data may not persist.");
            }
        }
    }

    @Override
    public void initializeOtpQueue() {
        refillOtpQueue();
        scheduler.scheduleAtFixedRate(this::checkAndRefillOtpQueue, 0, 10, TimeUnit.SECONDS);
        // Hook backup OTPs when server shuts down
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🔌 Server is shutting down, backing up OTPs...");
            logProducer.sendLog(
                    "user-service",
                    "WARN",
                    "[BACK UP OTP] 🔌 Server is shutting down, backing up OTPs...");
            backupOtpScheduler.backupOtpsToFile();
        }));
    }

    @Override
    public void checkAndRefillOtpQueue() {
        if (scheduler.isShutdown() || scheduler.isTerminated()) {
            throw new AppException(AuthErrorCode.SCHEDULER_NOT_RUN);
        }
        try (Jedis jedis = jedisPool.getResource()) {
            long queueSize = jedis.llen(OTP_QUEUE_KEY);
            if (queueSize < OTP_QUEUE_MIN_SIZE) {
                if (jedis.setnx("otp:refill:lock", "locked") == 1) {
                    try {
                        jedis.expire("otp:refill:lock", 10);
                        if (jedis.llen(OTP_QUEUE_KEY) < OTP_QUEUE_MIN_SIZE) {
                            List<String> newOtps = generateOtps(OTP_QUEUE_REFILL_SIZE);
                            for (String otp : newOtps) {
                                jedis.lpush(OTP_QUEUE_KEY, otp);
                                jedis.sadd(OTP_SET_KEY, otp);
                                jedis.expire(OTP_SET_KEY, OTP_EXPIRY_SECONDS);
                            }
                        }
                    } finally {
                        jedis.del("otp:refill:lock");
                    }
                }
            }
            cleanupExpiredOtps(jedis);
        }
    }

    private void cleanupExpiredOtps(Jedis jedis) {
        Set<String> usedOtps = jedis.smembers(OTP_SET_KEY);
        for (String otp : usedOtps) {
            if (!jedis.lrange(OTP_QUEUE_KEY, 0, -1).contains(otp)) {
                jedis.srem(OTP_SET_KEY, otp);
            }
        }
    }

    private void refillOtpQueue() {
        try (Jedis jedis = jedisPool.getResource()) {
            long queueSize = jedis.llen(OTP_QUEUE_KEY);

            if (Files.exists(OTP_BACKUP_PATH)) {
                System.out.println("♻️ Restoring OTPs from backup file (overwriting Redis)");
                logProducer.sendLog("user-service", "INFO", "[BACK UP OTP] ♻️ Restoring OTPs from backup file (overwriting Redis)");

                //Xoá dữ liệu cũ để ghi đè hoàn toàn
                jedis.del(OTP_QUEUE_KEY);
                jedis.del(OTP_SET_KEY);

                //Đọc từ file và ghi lại
                List<String> otps = objectMapper.readValue(
                        OTP_BACKUP_PATH.toFile(),
                        new TypeReference<List<String>>() {});

                for (String otp : otps) {
                    jedis.lpush(OTP_QUEUE_KEY, otp);
                    jedis.sadd(OTP_SET_KEY, otp);
                }

                jedis.expire(OTP_SET_KEY, OTP_EXPIRY_SECONDS);
            }
            else if (queueSize < OTP_QUEUE_MIN_SIZE) {
                List<String> newOtps = generateOtps(OTP_QUEUE_REFILL_SIZE);
                for (String otp : newOtps) {
                    jedis.lpush(OTP_QUEUE_KEY, otp);
                    jedis.sadd(OTP_SET_KEY, otp);
                }
                jedis.expire(OTP_SET_KEY, OTP_EXPIRY_SECONDS);
            }

            cleanupExpiredOtps(jedis);
        } catch (Exception e) {
            System.err.println("❌ Refill or restore failed: " + e.getMessage());
            logProducer.sendLog("user-service", "ERROR", "[BACK UP OTP] ❌ Refill or restore failed: " + e.getMessage());
        }
    }

    private List<String> generateOtps(int count) {
        List<String> otps = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        try (Jedis jedis = jedisPool.getResource()) {
            for (int i = 0; i < count; i++) {
                String otp;
                do {
                    StringBuilder otpBuilder = new StringBuilder();
                    for (int j = 0; j < OTP_LENGTH; j++) {
                        otpBuilder.append(random.nextInt(10));
                    }
                    otp = otpBuilder.toString();
                } while (jedis.sismember(OTP_SET_KEY, otp));
                otps.add(otp);
            }
        }
        return otps;
    }
}