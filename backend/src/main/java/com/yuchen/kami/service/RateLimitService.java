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
        checkLimit("redeem:" + identifier, properties.getRedeem().getRateLimitPerMinute(), Duration.ofMinutes(1));
    }

    public void checkLoginLimit(String ip, String username) {
        int limit = properties.getSecurity().getLoginRateLimitPerMinute();
        checkLimit("auth:login:ip:" + ip, limit, Duration.ofMinutes(1));
        if (username != null && !username.isBlank()) {
            checkLimit("auth:login:user:" + username.toLowerCase(), limit, Duration.ofMinutes(1));
        }
    }

    public void checkRegisterLimit(String ip) {
        int limit = properties.getSecurity().getRegisterRateLimitPerMinute();
        checkLimit("auth:register:ip:" + ip, limit, Duration.ofMinutes(1));
    }

    /** 邮件测试：每管理员每小时最多 5 次，每 IP 每小时最多 10 次 */
    public void checkMailTestLimit(Long userId, String ip) {
        checkLimit("mail:test:user:" + userId, 5, Duration.ofHours(1));
        if (ip != null && !ip.isBlank()) {
            checkLimit("mail:test:ip:" + ip, 10, Duration.ofHours(1));
        }
    }

    private void checkLimit(String scope, int maxPerWindow, Duration window) {
        String key = "rate:" + scope;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, window);
        }
        if (count != null && count > maxPerWindow) {
            throw new BusinessException(429, "请求过于频繁，请稍后重试");
        }
    }
}
