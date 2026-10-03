package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ShopUserProfileVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}
