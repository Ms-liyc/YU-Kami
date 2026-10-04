package com.yuchen.kami.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardTrends {

    private List<TrendDay> days;

    @Data
    @AllArgsConstructor
    public static class TrendDay {
        private String date;
        private long orderCount;
        private long redeemCount;
    }
}
