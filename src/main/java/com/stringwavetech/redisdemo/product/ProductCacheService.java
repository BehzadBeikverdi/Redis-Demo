package com.stringwavetech.redisdemo.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PREFIX = "product:";

    public void save(Product product) {
        try {
            String key = PREFIX + product.getId();

            String json = objectMapper.writeValueAsString(product);

            redisTemplate.opsForValue()
                    .set(key, json, Duration.ofMinutes(5));

        } catch (Exception e) {
            throw new RuntimeException("Could not cache product", e);
        }
    }

    public Product get(UUID id) {
        try {
            String key = PREFIX + id;

            String json = redisTemplate.opsForValue().get(key);

            if (json == null) {
                return null;
            }

            return objectMapper.readValue(json, Product.class);

        } catch (Exception e) {
            throw new RuntimeException("Could not read product from cache", e);
        }
    }
}