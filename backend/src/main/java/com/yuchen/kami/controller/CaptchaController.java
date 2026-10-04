package com.yuchen.kami.controller;

import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.CaptchaResponse;
import com.yuchen.kami.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping
    public Result<CaptchaResponse> captcha() {
        return Result.ok(captchaService.generate());
    }
}
