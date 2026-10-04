package com.yuchen.kami.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.DashboardStats;
import com.yuchen.kami.dto.DashboardTrends;
import com.yuchen.kami.entity.RedeemRecord;
import com.yuchen.kami.mapper.RedeemRecordMapper;
import com.yuchen.kami.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final RedeemRecordMapper redeemRecordMapper;

    @GetMapping("/dashboard")
    public Result<DashboardStats> stats() {
        return Result.ok(dashboardService.getStats());
    }

    @GetMapping("/dashboard/trends")
    public Result<DashboardTrends> trends() {
        return Result.ok(dashboardService.getTrends());
    }

    @GetMapping("/redeem-records")
    public Result<PageResult<RedeemRecord>> redeemRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<RedeemRecord> result = redeemRecordMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<RedeemRecord>().orderByDesc(RedeemRecord::getCreatedAt));
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, size));
    }
}
