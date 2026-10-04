package com.yuchen.kami.service;

import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.dto.TotpSetupResponse;
import com.yuchen.kami.entity.SysUser;
import com.yuchen.kami.mapper.SysUserMapper;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TotpService {

    private static final String SETUP_PREFIX = "totp:setup:";
    private static final Duration SETUP_TTL = Duration.ofMinutes(10);

    private final SysUserMapper sysUserMapper;
    private final StringRedisTemplate redisTemplate;
    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();

    public TotpSetupResponse setup(Long userId) {
        SysUser user = requireUser(userId);
        if (user.getTotpEnabled() != null && user.getTotpEnabled() == 1) {
            throw new BusinessException("双因素认证已启用");
        }
        String secret = secretGenerator.generate();
        redisTemplate.opsForValue().set(SETUP_PREFIX + userId, secret, SETUP_TTL);
        String qrCodeUrl = buildQrCodeDataUri(user.getUsername(), secret);
        return new TotpSetupResponse(secret, qrCodeUrl);
    }

    public void enable(Long userId, String totpCode) {
        SysUser user = requireUser(userId);
        String secret = redisTemplate.opsForValue().get(SETUP_PREFIX + userId);
        if (secret == null || secret.isBlank()) {
            throw new BusinessException("请先获取 TOTP 设置信息");
        }
        if (!verifyCode(secret, totpCode)) {
            throw new BusinessException("验证码不正确");
        }
        user.setTotpSecret(secret);
        user.setTotpEnabled(1);
        sysUserMapper.updateById(user);
        redisTemplate.delete(SETUP_PREFIX + userId);
    }

    public void disable(Long userId, String totpCode) {
        SysUser user = requireUser(userId);
        if (user.getTotpEnabled() == null || user.getTotpEnabled() != 1) {
            throw new BusinessException("双因素认证未启用");
        }
        if (!verifyCode(user.getTotpSecret(), totpCode)) {
            throw new BusinessException("验证码不正确");
        }
        user.setTotpSecret(null);
        user.setTotpEnabled(0);
        sysUserMapper.updateById(user);
    }

    public boolean isEnabled(Long userId) {
        SysUser user = requireUser(userId);
        return user.getTotpEnabled() != null && user.getTotpEnabled() == 1;
    }

    public void verifyLogin(SysUser user, String totpCode) {
        if (user.getTotpEnabled() == null || user.getTotpEnabled() != 1) {
            return;
        }
        if (totpCode == null || totpCode.isBlank()) {
            throw new BusinessException("请输入双因素验证码");
        }
        if (!verifyCode(user.getTotpSecret(), totpCode)) {
            throw new BusinessException("双因素验证码不正确");
        }
    }

    private boolean verifyCode(String secret, String code) {
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(
                new DefaultCodeGenerator(HashingAlgorithm.SHA1), new SystemTimeProvider());
        return verifier.isValidCode(secret, code);
    }

    private String buildQrCodeDataUri(String username, String secret) {
        try {
            QrData data = new QrData.Builder()
                    .label(username)
                    .secret(secret)
                    .issuer("YU-Kami")
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();
            byte[] image = new ZxingPngQrGenerator().generate(data);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(image);
        } catch (Exception e) {
            throw new BusinessException("二维码生成失败");
        }
    }

    private SysUser requireUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
