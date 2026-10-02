package com.yuchen.kami.controller;

import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.ChangePasswordRequest;
import com.yuchen.kami.dto.LoginRequest;
import com.yuchen.kami.dto.LoginResponse;
import com.yuchen.kami.dto.ShopProfileUpdateRequest;
import com.yuchen.kami.dto.ShopRegisterRequest;
import com.yuchen.kami.dto.ShopUserProfileVO;
import com.yuchen.kami.service.ShopAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop/auth")
@RequiredArgsConstructor
public class ShopAuthController {

    private final ShopAuthService shopAuthService;

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody ShopRegisterRequest request) {
        return Result.ok(shopAuthService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(shopAuthService.login(request));
    }

    @GetMapping("/me")
    public Result<ShopUserProfileVO> me(Authentication auth) {
        return Result.ok(shopAuthService.getProfile((Long) auth.getDetails()));
    }

    @PutMapping("/profile")
    public Result<ShopUserProfileVO> updateProfile(@Valid @RequestBody ShopProfileUpdateRequest request,
                                                   Authentication auth) {
        return Result.ok(shopAuthService.updateProfile((Long) auth.getDetails(), request));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                       Authentication auth) {
        shopAuthService.changePassword((Long) auth.getDetails(), request);
        return Result.ok();
    }
}
