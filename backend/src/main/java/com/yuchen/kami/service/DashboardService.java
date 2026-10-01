package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.dto.DashboardStats;
import com.yuchen.kami.entity.CardBatch;
import com.yuchen.kami.entity.CardKey;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.entity.RedeemRecord;
import com.yuchen.kami.mapper.CardBatchMapper;
import com.yuchen.kami.mapper.CardKeyMapper;
import com.yuchen.kami.mapper.ProductMapper;
import com.yuchen.kami.mapper.RedeemRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CardKeyMapper cardKeyMapper;
    private final ProductMapper productMapper;
    private final CardBatchMapper cardBatchMapper;
    private final RedeemRecordMapper redeemRecordMapper;

    public DashboardStats getStats() {
        long total = cardKeyMapper.selectCount(null);
        long used = cardKeyMapper.selectCount(new LambdaQueryWrapper<CardKey>()
                .eq(CardKey::getStatus, CardKey.STATUS_USED));
        long unused = cardKeyMapper.selectCount(new LambdaQueryWrapper<CardKey>()
                .eq(CardKey::getStatus, CardKey.STATUS_UNUSED));
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayRedeems = redeemRecordMapper.selectCount(new LambdaQueryWrapper<RedeemRecord>()
                .eq(RedeemRecord::getResult, "SUCCESS")
                .ge(RedeemRecord::getCreatedAt, startOfDay));

        return DashboardStats.builder()
                .totalCards(total)
                .usedCards(used)
                .unusedCards(unused)
                .todayRedeems(todayRedeems)
                .totalProducts(productMapper.selectCount(new LambdaQueryWrapper<Product>()))
                .totalBatches(cardBatchMapper.selectCount(new LambdaQueryWrapper<CardBatch>()))
                .build();
    }
}
