package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SetupStatusDTO {

    /** 是否仅使用模拟支付（未对接真实渠道） */
    private boolean mockOnly;

    /** 支付宝是否已配置并启用 */
    private boolean alipayReady;

    /** 微信是否已配置并启用 */
    private boolean wechatReady;

    /** 是否需要在管理端引导用户自行对接支付 */
    private boolean needsPaymentSetup;

    /** 当前回调基础地址（来自环境变量，部署者可自行修改） */
    private String paymentBaseUrl;

    /** 支付完成跳转页 */
    private String paymentReturnUrl;

    /** 配置文档路径 */
    private String paymentGuidePath;

    /** SMTP 邮件是否已配置 */
    private boolean mailConfigured;

    /** 是否已配置库存告警收件人（不暴露具体邮箱） */
    private boolean hasStockAlertRecipient;
}
