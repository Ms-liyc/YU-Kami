package com.yuchen.kami.dto;

import com.yuchen.kami.dto.ProductStockAlert;
import com.yuchen.kami.entity.RedeemRecord;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardStats {

    private long totalCards;
    private long usedCards;
    private long unusedCards;
    private long todayRedeems;
    private long todayOrders;
    private long totalProducts;
    private long totalBatches;
    private double usageRate;
    private int stockLowThreshold;
    private List<RedeemRecord> recentRedeems;
    private List<ProductStockAlert> lowStockProducts;
}
