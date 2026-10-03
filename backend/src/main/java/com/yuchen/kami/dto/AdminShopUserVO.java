package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminShopUserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private BigDecimal balance;
    private Integer status;
    private LocalDateTime createdAt;
}
