package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.dto.ChangePasswordRequest;
import com.yuchen.kami.dto.LoginRequest;
import com.yuchen.kami.dto.LoginResponse;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.entity.SysUser;
import com.yuchen.kami.mapper.SysUserMapper;
import com.yuchen.kami.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;
    private final CaptchaService captchaService;
    private final TotpService totpService;

    public LoginResponse login(LoginRequest request, String ip) {
        rateLimitService.checkLoginLimit(ip, request.getUsername());
        captchaService.validate(request.getCaptchaId(), request.getCaptchaCode());
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || user.getStatus() != 1) {
            throw new org.springframework.security.authentication.BadCredentialsException("认证失败");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException("认证失败");
        }
        totpService.verifyLogin(user, request.getTotpCode());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        auditService.log(user.getId(), user.getUsername(), "LOGIN", "sys_user", "管理员登录", ip);
        boolean warnDefault = isDefaultPassword(user.getPassword());
        return new LoginResponse(token, user.getUsername(), user.getNickname(), user.getRole(), warnDefault);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("当前密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(user);
        auditService.log(userId, user.getUsername(), "CHANGE_PASSWORD", "sys_user", "修改登录密码", null);
    }

    private boolean isDefaultPassword(String encodedPassword) {
        return passwordEncoder.matches("admin123", encodedPassword);
    }
}
