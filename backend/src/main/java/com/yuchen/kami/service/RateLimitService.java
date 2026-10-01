package com.yuchen.kami.service;

import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.config.YuKamiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final YuKamiProperties properties;

    public void checkRedeemLimit(String identifier) {
        String key = "rate:redeem:" + identifier;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        if (count != null && count > properties.getRedeem().getRateLimitPerMinute()) {
            throw new BusinessException(429, "请求过于频繁，请稍后重试");
        }
    }
}
