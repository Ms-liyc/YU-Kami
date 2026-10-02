package com.yuchen.kami.controller;

import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.RecentOrderItem;
import com.yuchen.kami.dto.ShopPublicStats;
import com.yuchen.kami.service.ShopStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopStatsController {

    private final ShopStatsService shopStatsService;

    @GetMapping("/stats")
    public Result<ShopPublicStats> stats() {
        return Result.ok(shopStatsService.getPublicStats());
    }

    @GetMapping("/orders/recent")
    public Result<List<RecentOrderItem>> recent(@RequestParam(defaultValue = "8") int limit) {
        return Result.ok(shopStatsService.recentDelivered(limit));
    }
}
