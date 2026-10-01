package com.yuchen.kami.controller;

import com.yuchen.kami.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentNotifyController {

    private final PaymentService paymentService;

    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        log.info("支付宝回调: orderNo={}", params.get("out_trade_no"));
        if (paymentService.handleAlipayNotify(params)) {
            return "success";
        }
        return "failure";
    }

    @PostMapping("/wechat/notify")
    public Map<String, String> wechatNotify(
            @RequestBody String body,
            @RequestHeader(value = "Wechatpay-Serial", required = false) String serial,
            @RequestHeader(value = "Wechatpay-Nonce", required = false) String nonce,
            @RequestHeader(value = "Wechatpay-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "Wechatpay-Signature", required = false) String signature) {
        log.info("微信支付回调");
        try {
            paymentService.handleWechatNotify(body, serial, nonce, timestamp, signature);
            return Map.of("code", "SUCCESS", "message", "成功");
        } catch (Exception e) {
            log.error("微信支付回调处理失败", e);
            return Map.of("code", "FAIL", "message", "失败");
        }
    }
}
