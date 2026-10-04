package com.yuchen.kami.controller;

import com.yuchen.kami.common.ClientIpUtils;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.ChangePasswordRequest;
import com.yuchen.kami.dto.ForgotPasswordRequest;
import com.yuchen.kami.dto.LoginRequest;
import com.yuchen.kami.dto.LoginResponse;
import com.yuchen.kami.dto.ResetPasswordRequest;
import com.yuchen.kami.dto.ShopProfileUpdateRequest;
import com.yuchen.kami.dto.ShopRegisterRequest;
import com.yuchen.kami.dto.ShopUserProfileVO;
import com.yuchen.kami.service.PasswordResetService;
import com.yuchen.kami.service.ShopAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shop/auth")
@RequiredArgsConstructor
public class ShopAuthController {

    private final ShopAuthService shopAuthService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody ShopRegisterRequest request,
                                          HttpServletRequest httpRequest) {
        return Result.ok(shopAuthService.register(request, ClientIpUtils.resolve(httpRequest)));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        return Result.ok(shopAuthService.login(request, ClientIpUtils.resolve(httpRequest)));
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

    @PostMapping("/resend-verify-email")
    public Result<Void> resendVerifyEmail(Authentication auth) {
        shopAuthService.resendVerificationEmail((Long) auth.getDetails());
        return Result.ok();
    }

    @GetMapping("/verify-email")
    public Result<Void> verifyEmail(@RequestParam String token) {
        shopAuthService.verifyEmail(token);
        return Result.ok();
    }

    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestShopReset(request.getUsername());
        return Result.ok();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetShopPassword(request.getToken(), request.getNewPassword());
        return Result.ok();
    }
}
