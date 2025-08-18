package com.example.user_service.scheduler;

import com.example.user_service.kafka.producer.ProducerLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class BackupOtpScheduler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JedisPool jedisPool;
    private final ProducerLog logProducer;

    private static final String OTP_QUEUE_KEY = "otp:queue";
    private static final Path OTP_BACKUP_PATH = Paths.get("backup/otp_backup.json");

    public BackupOtpScheduler(JedisPool jedisPool, ProducerLog logProducer) {
        this.jedisPool = jedisPool;
        this.logProducer = logProducer;
    }

    @Scheduled(fixedRate = 60_000)
    public void backupOtpsToFile() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> otps = jedis.lrange(OTP_QUEUE_KEY, 0, -1);
            Files.createDirectories(OTP_BACKUP_PATH.getParent());
            objectMapper.writeValue(OTP_BACKUP_PATH.toFile(), otps);
            System.out.println("💾 Backed up " + otps.size() + " OTPs to " + OTP_BACKUP_PATH);
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[BACK UP OTP] OTP backup failed: " + e.getMessage());
        }
    }
}
