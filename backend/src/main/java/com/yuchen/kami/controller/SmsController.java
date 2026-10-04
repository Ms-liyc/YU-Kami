package com.yuchen.kami.controller;

import com.yuchen.kami.common.ClientIpUtils;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.SmsStatusDTO;
import com.yuchen.kami.dto.SmsTestRequest;
import com.yuchen.kami.service.RateLimitService;
import com.yuchen.kami.service.SmsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SmsController {

    private final SmsService smsService;
    private final RateLimitService rateLimitService;

    @GetMapping("/status")
    public Result<SmsStatusDTO> status() {
        return Result.ok(smsService.getStatus());
    }

    @PostMapping("/test")
    public Result<Void> sendTest(@Valid @RequestBody SmsTestRequest request,
                                 Authentication auth,
                                 HttpServletRequest httpRequest) {
        Long userId = (Long) auth.getDetails();
        rateLimitService.checkSmsTestLimit(userId, ClientIpUtils.resolve(httpRequest));
        smsService.sendTestSms(request.getPhone());
        return Result.ok();
    }
}
