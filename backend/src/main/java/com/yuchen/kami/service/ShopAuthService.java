package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.dto.LoginRequest;
import com.yuchen.kami.dto.LoginResponse;
import com.yuchen.kami.dto.ShopRegisterRequest;
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

    public LoginResponse register(ShopRegisterRequest request) {
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
        user.setStatus(1);
        shopUserMapper.insert(user);
        return buildLoginResponse(user);
    }

    public LoginResponse login(LoginRequest request) {
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

    private LoginResponse buildLoginResponse(ShopUser user) {
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), "SHOP_USER");
        return new LoginResponse(token, user.getUsername(), user.getNickname(), "SHOP_USER");
    }
}
