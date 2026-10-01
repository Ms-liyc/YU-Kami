package com.yuchen.kami.controller;

import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.LoginRequest;
import com.yuchen.kami.dto.LoginResponse;
import com.yuchen.kami.dto.ShopRegisterRequest;
import com.yuchen.kami.service.ShopAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
}
