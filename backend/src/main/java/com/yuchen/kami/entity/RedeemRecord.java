package com.yuchen.kami.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("redeem_record")
public class RedeemRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long cardId;
    private Long productId;
    private Long batchId;
    private String redeemUser;
    private String redeemIp;
    private String userAgent;
    private String result;
    private String message;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
