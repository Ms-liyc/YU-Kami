package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MailStatusDTO {

    /** SMTP 是否已正确配置（不返回 host/账号/密码） */
    private boolean configured;

    /** 是否使用 QQ 邮箱 SMTP */
    private boolean qqMail;

    /** 是否已配置库存告警收件人（不返回具体邮箱地址） */
    private boolean hasStockAlertRecipient;
}
