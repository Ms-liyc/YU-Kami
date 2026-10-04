package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.entity.ShopUser;
import com.yuchen.kami.entity.SysUser;
import com.yuchen.kami.mapper.ShopUserMapper;
import com.yuchen.kami.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String ADMIN_TOKEN_PREFIX = "pwd-reset:admin:";
    private static final String SHOP_TOKEN_PREFIX = "pwd-reset:shop:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    private final SysUserMapper sysUserMapper;
    private final ShopUserMapper shopUserMapper;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final YuKamiProperties properties;

    public void requestAdminReset(String username) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            return;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(ADMIN_TOKEN_PREFIX + token, String.valueOf(user.getId()), TOKEN_TTL);
        String url = appUrl() + "/reset-password?token=" + token;
        emailService.sendResetEmail(resolveAdminEmail(user), url);
    }

    public void requestShopReset(String username) {
        ShopUser user = shopUserMapper.selectOne(new LambdaQueryWrapper<ShopUser>()
                .eq(ShopUser::getUsername, username));
        if (user == null) {
            return;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(SHOP_TOKEN_PREFIX + token, String.valueOf(user.getId()), TOKEN_TTL);
        String url = appUrl() + "/shop/reset-password?token=" + token;
        emailService.sendResetEmail(user.getEmail(), url);
    }

    public void resetAdminPassword(String token, String newPassword) {
        Long userId = resolveToken(ADMIN_TOKEN_PREFIX, token);
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
    }

    public void resetShopPassword(String token, String newPassword) {
        Long userId = resolveToken(SHOP_TOKEN_PREFIX, token);
        ShopUser user = shopUserMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        shopUserMapper.updateById(user);
    }

    private Long resolveToken(String prefix, String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("重置令牌无效");
        }
        String key = prefix + token;
        String userId = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        if (userId == null) {
            throw new BusinessException("重置令牌无效或已过期");
        }
        return Long.parseLong(userId);
    }

    private String appUrl() {
        String url = properties.getMail().getAppUrl();
        return url != null && !url.isBlank() ? url.replaceAll("/$", "") : "http://localhost:5173";
    }

    private String resolveAdminEmail(SysUser user) {
        return user.getUsername() != null && user.getUsername().contains("@") ? user.getUsername() : null;
    }
}
