package com.yuchen.kami.dto;

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
    private long totalProducts;
    private long totalBatches;
    private double usageRate;
    private List<RedeemRecord> recentRedeems;
}
