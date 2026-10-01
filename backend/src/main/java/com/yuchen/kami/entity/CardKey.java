package com.yuchen.kami.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("card_key")
public class CardKey {

    public static final int STATUS_UNUSED = 0;
    public static final int STATUS_USED = 1;
    public static final int STATUS_REVOKED = 2;
    public static final int STATUS_EXPIRED = 3;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long batchId;
    private Long productId;
    private String keyHash;
    private String keyPepper;
    private String keyChecksum;
    private String encryptedMeta;
    private Integer status;
    private String redeemUser;
    private String redeemIp;
    private LocalDateTime redeemAt;
    private LocalDateTime expireAt;
    @Version
    private Integer version;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
