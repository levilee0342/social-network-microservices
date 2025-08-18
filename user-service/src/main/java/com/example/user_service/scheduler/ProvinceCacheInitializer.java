package com.example.user_service.scheduler;

import com.example.user_service.kafka.producer.ProducerLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.List;

@Service
public class ProvinceCacheInitializer {

    private static final String PROVINCE_KEY = "province:lists";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JedisPool jedisPool;
    private final ProducerLog logProducer;

    public ProvinceCacheInitializer(JedisPool jedisPool, ProducerLog logProducer) {
        this.jedisPool = jedisPool;
        this.logProducer = logProducer;
    }

    @PostConstruct
    public void syncProvincesToRedis() {
        try (Jedis jedis = jedisPool.getResource()) {
            if (!jedis.exists(PROVINCE_KEY)) {
                List<String> provinces = Arrays.asList(
                        "Tuyên Quang", "Cao Bằng", "Lai Châu", "Lào Cai", "Thái Nguyên", "Điện Biên", "Lạng Sơn", "Sơn La",
                        "Phú Thọ", "Bắc Ninh", "Quảng Ninh", "Thành Phố Hà Nội", "Thành Phố Hải Phòng", "Hưng Yên", "Ninh Bình", "Thanh Hóa",
                        "Nghệ An", "Hà Tĩnh", "Quảng Trị", "Thành Phố Huế", "Thành Phố Đà Nẵng", "Quảng Ngãi", "Gia Lai", "Đắk Lắk",
                        "Khánh Hoà", "Lâm Đồng", "Đồng Nai", "Tây Ninh", "Thành Phố Hồ Chí Minh", "Đồng Tháp", "An Giang", "Vĩnh Long",
                        "Thành Phố Cần Thơ", "Cà Mau"
                );

                String json = objectMapper.writeValueAsString(provinces);
                jedis.set(PROVINCE_KEY, json);
                System.out.println("Synced provinces to Redis with key: " + PROVINCE_KEY);
            } else {
                System.out.println("Provinces already exist in Redis. Skipping sync.");
            }
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[SYNC PROVINCES] Failed to sync provinces: " + e.getMessage());
        }
    }
}
