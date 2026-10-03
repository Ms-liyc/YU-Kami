package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.common.BusinessException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopAuthService {

    private final ShopUserMapper shopUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RateLimitService rateLimitService;

    public LoginResponse register(ShopRegisterRequest request, String ip) {
        rateLimitService.checkRegisterLimit(ip);
        Long count = shopUserMapper.selectCount(new LambdaQueryWrapper<ShopUser>()
                .eq(ShopUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new com.yuchen.kami.common.BusinessException("用户名已存在");
        }
        ShopUser user = new ShopUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setBalance(java.math.BigDecimal.ZERO);
        user.setStatus(1);
        shopUserMapper.insert(user);
        return buildLoginResponse(user);
    }

    public LoginResponse login(LoginRequest request, String ip) {
        rateLimitService.checkLoginLimit(ip, request.getUsername());
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
        }
        shopUserMapper.updateById(user);
        return getProfile(userId);
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

    private LoginResponse buildLoginResponse(ShopUser user) {
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), "SHOP_USER");
        return new LoginResponse(token, user.getUsername(), user.getNickname(), "SHOP_USER", false);
    }
}
