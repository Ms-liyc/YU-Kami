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
@TableName("promotion")
public class Promotion {

    public static final String KIND_ACTIVITY = "ACTIVITY";
    public static final String KIND_COUPON = "COUPON";

    public static final String TYPE_PERCENT_OFF = "PERCENT_OFF";
    public static final String TYPE_FIXED_OFF = "FIXED_OFF";
    public static final String TYPE_OVERRIDE_PRICE = "OVERRIDE_PRICE";

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** ACTIVITY=促销活动 COUPON=优惠券 */
    private String kind;
    /** 优惠券码，仅 kind=COUPON 时有值 */
    private String code;
    private String name;
    private String description;
    private String type;
    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private BigDecimal maxDiscount;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    /** 适用商品 ID，逗号分隔；空表示全场 */
    private String productIds;
    private Integer isHoliday;
    private Integer priority;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer perUserLimit;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
