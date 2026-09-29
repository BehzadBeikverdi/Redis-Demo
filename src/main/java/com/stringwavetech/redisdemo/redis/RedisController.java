package com.stringwavetech.redisdemo.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    @PostMapping
    public String set(@RequestParam String key, @RequestParam String value) {
        redisService.set(key, value);
        return "Saved";
    }

    @PostMapping("/ttl")
    public String setWithTtl(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam long seconds
    ) {
        redisService.set(key, value, seconds);
        return "Saved with TTL";
    }

    @GetMapping
    public String get(@RequestParam String key) {
        return redisService.get(key);
    }

    @GetMapping("/ttl")
    public Long getTtl(@RequestParam String key) {
        return redisService.getTtl(key);
    }

    @DeleteMapping
    public String delete(@RequestParam String key) {
        redisService.delete(key);
        return "Deleted";
    }
}
