package com.yuchen.kami.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shop_order")
public class ShopOrder {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String orderNo;
    private Long userId;
    private Long productId;
    private String productName;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private String couponCode;
    private Long promotionId;
    private Integer quantity;
    private String status;
    private String paymentMethod;
    private String paymentNo;
    private Long cardId;
    private String remark;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime deliveredAt;
}
