package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopPublicStats {
    private long totalOrders;
    private long totalUsers;
    private long cardsDelivered;
    private double uptime;
}
