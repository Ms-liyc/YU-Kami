package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrepayResponse {

    /** INSTANT=即时到账, REDIRECT=跳转支付, QRCODE=扫码支付 */
    private String payType;
    private Long orderId;
    private String orderNo;
    private String payUrl;
    private String payForm;
    private String codeUrl;
    private String cardKey;
    private String message;
}
