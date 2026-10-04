package com.yuchen.kami.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long productId;
    private String productName;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private String couponCode;
    private String promotionName;
    private Integer quantity;
    private String status;
    private String statusLabel;
    private String orderType;
    private String paymentMethod;
    private String cardKey;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime deliveredAt;
    private String username;
}
