package com.yuchen.kami.controller;

import com.yuchen.kami.common.ClientIpUtils;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.ChangePasswordRequest;
import com.yuchen.kami.dto.ForgotPasswordRequest;
import com.yuchen.kami.dto.LoginRequest;
import com.yuchen.kami.dto.LoginResponse;
import com.yuchen.kami.dto.ResetPasswordRequest;
import com.yuchen.kami.dto.TotpEnableRequest;
import com.yuchen.kami.dto.TotpSetupResponse;
import com.yuchen.kami.dto.TotpStatusResponse;
import com.yuchen.kami.service.AuthService;
import com.yuchen.kami.service.PasswordResetService;
import com.yuchen.kami.service.TotpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TotpService totpService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = ClientIpUtils.resolve(httpRequest);
        return Result.ok(authService.login(request, ip));
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication auth) {
        authService.changePassword((Long) auth.getDetails(), request);
        return Result.ok();
    }

    @GetMapping("/totp/status")
    public Result<TotpStatusResponse> totpStatus(Authentication auth) {
        return Result.ok(new TotpStatusResponse(totpService.isEnabled((Long) auth.getDetails())));
    }

    @GetMapping("/totp/setup")
    public Result<TotpSetupResponse> totpSetup(Authentication auth) {
        return Result.ok(totpService.setup((Long) auth.getDetails()));
    }

    @PostMapping("/totp/enable")
    public Result<Void> totpEnable(@Valid @RequestBody TotpEnableRequest request, Authentication auth) {
        totpService.enable((Long) auth.getDetails(), request.getTotpCode());
        return Result.ok();
    }

    @PostMapping("/totp/disable")
    public Result<Void> totpDisable(@Valid @RequestBody TotpEnableRequest request, Authentication auth) {
        totpService.disable((Long) auth.getDetails(), request.getTotpCode());
        return Result.ok();
    }

    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestAdminReset(request.getUsername());
        return Result.ok();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetAdminPassword(request.getToken(), request.getNewPassword());
        return Result.ok();
    }
}
