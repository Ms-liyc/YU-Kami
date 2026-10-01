package com.yuchen.kami.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayOrderRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /** 微信 JSAPI 支付 openid（微信内浏览器授权后获取） */
    private String openid;

    /** 是否使用微信 JSAPI（微信内浏览器建议 true） */
    private Boolean wechatJsapi;
}
