package com.stringwavetech.redisdemo.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long seconds) {
        stringRedisTemplate
                .opsForValue()
                .set(key, value, Duration.ofSeconds(seconds));
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public Long getTtl(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }
}
