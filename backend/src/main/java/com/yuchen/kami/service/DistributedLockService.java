package com.yuchen.kami.service;

import com.yuchen.kami.config.YuKamiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final StringRedisTemplate redisTemplate;
    private final YuKamiProperties properties;

    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        String token = UUID.randomUUID().toString();
        String key = "lock:" + lockKey;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, token, Duration.ofSeconds(properties.getRedeem().getLockTtlSeconds()));
        if (!Boolean.TRUE.equals(acquired)) {
            throw new com.yuchen.kami.common.BusinessException(409, "卡密正在处理中，请稍后重试");
        }
        try {
            return action.get();
        } finally {
            String current = redisTemplate.opsForValue().get(key);
            if (token.equals(current)) {
                redisTemplate.delete(key);
            }
        }
    }
}
