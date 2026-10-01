package com.yuchen.kami.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.mapper.PaymentConfigMapper;
import com.yuchen.kami.payment.WechatOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/shop/payment/wechat")
@RequiredArgsConstructor
public class ShopWechatPayController {

    private final PaymentConfigMapper paymentConfigMapper;
    private final WechatOAuthService wechatOAuthService;

    @GetMapping("/oauth-url")
    public Result<Map<String, String>> oauthUrl(
            @RequestParam Long orderId,
            @RequestParam(defaultValue = "/shop/orders") String redirect,
            Authentication auth) {
        PaymentConfig config = getWechatConfig();
        Long userId = (Long) auth.getDetails();
        String url = wechatOAuthService.buildOAuthUrl(config, redirect, orderId, userId);
        return Result.ok(Map.of("url", url));
    }

    @GetMapping("/oauth-callback")
    public void oauthCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state,
            HttpServletResponse response) throws IOException {
        PaymentConfig config = getWechatConfig();
        Long uid = null;
        if (state != null && state.contains("|")) {
            try {
                uid = Long.parseLong(state.split("\\|", 3)[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        if (uid == null) {
            response.sendError(400, "缺少用户标识");
            return;
        }
        String redirect = wechatOAuthService.handleCallback(config, code, state, uid);
        response.sendRedirect(redirect);
    }

    private PaymentConfig getWechatConfig() {
        PaymentConfig config = paymentConfigMapper.selectOne(new LambdaQueryWrapper<PaymentConfig>()
                .eq(PaymentConfig::getChannel, "WECHAT")
                .eq(PaymentConfig::getStatus, 1));
        if (config == null) {
            throw new BusinessException("微信支付未启用");
        }
        return config;
    }
}
