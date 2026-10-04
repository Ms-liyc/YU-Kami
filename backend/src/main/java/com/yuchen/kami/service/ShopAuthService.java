package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.dto.ChangePasswordRequest;
import com.yuchen.kami.dto.LoginRequest;
import com.yuchen.kami.dto.LoginResponse;
import com.yuchen.kami.dto.ShopProfileUpdateRequest;
import com.yuchen.kami.dto.ShopRegisterRequest;
import com.yuchen.kami.dto.ShopUserProfileVO;
import com.yuchen.kami.entity.ShopUser;
import com.yuchen.kami.mapper.ShopUserMapper;
import com.yuchen.kami.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopAuthService {

    private static final String EMAIL_VERIFY_PREFIX = "email-verify:";
    private static final Duration EMAIL_VERIFY_TTL = Duration.ofHours(24);

    private final ShopUserMapper shopUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RateLimitService rateLimitService;
    private final CaptchaService captchaService;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;
    private final YuKamiProperties properties;

    public LoginResponse register(ShopRegisterRequest request, String ip) {
        rateLimitService.checkRegisterLimit(ip);
        captchaService.validate(request.getCaptchaId(), request.getCaptchaCode());
        Long count = shopUserMapper.selectCount(new LambdaQueryWrapper<ShopUser>()
                .eq(ShopUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        ShopUser user = new ShopUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setBalance(java.math.BigDecimal.ZERO);
        user.setStatus(1);
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmailVerified(0);
        }
        shopUserMapper.insert(user);
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            sendVerificationEmail(user);
        }
        return buildLoginResponse(user);
    }

    public LoginResponse login(LoginRequest request, String ip) {
        rateLimitService.checkLoginLimit(ip, request.getUsername());
        captchaService.validate(request.getCaptchaId(), request.getCaptchaCode());
        ShopUser user = shopUserMapper.selectOne(new LambdaQueryWrapper<ShopUser>()
                .eq(ShopUser::getUsername, request.getUsername()));
        if (user == null || user.getStatus() != 1) {
            throw new org.springframework.security.authentication.BadCredentialsException("认证失败");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException("认证失败");
        }
        return buildLoginResponse(user);
    }

    public void verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("验证令牌无效");
        }
        String key = EMAIL_VERIFY_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        if (userId == null) {
            throw new BusinessException("验证令牌无效或已过期");
        }
        ShopUser user = shopUserMapper.selectById(Long.parseLong(userId));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setEmailVerified(1);
        shopUserMapper.updateById(user);
    }

    public ShopUserProfileVO getProfile(Long userId) {
        ShopUser user = shopUserMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在");
        }
        return ShopUserProfileVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .emailVerified(user.getEmailVerified() != null && user.getEmailVerified() == 1)
                .balance(user.getBalance() != null ? user.getBalance() : java.math.BigDecimal.ZERO)
                .createdAt(user.getCreatedAt())
                .build();
    }

    public ShopUserProfileVO updateProfile(Long userId, ShopProfileUpdateRequest request) {
        ShopUser user = shopUserMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在");
        }
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail().trim());
            user.setEmailVerified(0);
            sendVerificationEmail(user);
        }
        shopUserMapper.updateById(user);
        return getProfile(userId);
    }

    public void resendVerificationEmail(Long userId) {
        ShopUser user = shopUserMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BusinessException("请先填写邮箱");
        }
        if (user.getEmailVerified() != null && user.getEmailVerified() == 1) {
            throw new BusinessException("邮箱已验证");
        }
        sendVerificationEmail(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        ShopUser user = shopUserMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("当前密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        shopUserMapper.updateById(user);
    }

    private void sendVerificationEmail(ShopUser user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(EMAIL_VERIFY_PREFIX + token, String.valueOf(user.getId()), EMAIL_VERIFY_TTL);
        String appUrl = properties.getMail().getAppUrl();
        String base = appUrl != null && !appUrl.isBlank() ? appUrl.replaceAll("/$", "") : "http://localhost:8080";
        String verifyUrl = base + "/shop/verify-email?token=" + token;
        emailService.sendVerifyEmail(user.getEmail(), verifyUrl);
    }

    private LoginResponse buildLoginResponse(ShopUser user) {
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), "SHOP_USER");
        return new LoginResponse(token, user.getUsername(), user.getNickname(), "SHOP_USER", false);
    }
}
