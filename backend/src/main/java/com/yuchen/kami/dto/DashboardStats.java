package com.yuchen.kami.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStats {

    private long totalCards;
    private long usedCards;
    private long unusedCards;
    private long todayRedeems;
    private long totalProducts;
    private long totalBatches;
}
