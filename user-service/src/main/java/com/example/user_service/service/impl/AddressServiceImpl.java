package com.example.user_service.service.impl;

import com.example.user_service.service.interfaces.IAddressService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.List;

@Service
public class AddressServiceImpl implements IAddressService {
    private static final String PROVINCE_KEY = "province:lists";

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    public AddressServiceImpl(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.objectMapper = new ObjectMapper();
    }

    public List<String> getProvincesFromCache() {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(PROVINCE_KEY);
            if (json != null) {
                return objectMapper.readValue(json, new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace(); // hoặc log lỗi
        }
        return Collections.emptyList();
    }
}
