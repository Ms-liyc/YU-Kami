package com.yuchen.kami.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("webhook_log")
public class WebhookLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long webhookId;
    private String event;
    private String payload;
    private String response;
    private Integer statusCode;
    private Integer success;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
