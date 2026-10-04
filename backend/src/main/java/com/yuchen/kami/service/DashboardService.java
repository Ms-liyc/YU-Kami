package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.dto.DashboardStats;
import com.yuchen.kami.dto.ProductStockAlert;
import com.yuchen.kami.entity.CardBatch;
import com.yuchen.kami.entity.CardKey;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.entity.RedeemRecord;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.mapper.CardBatchMapper;
import com.yuchen.kami.mapper.CardKeyMapper;
import com.yuchen.kami.mapper.ProductMapper;
import com.yuchen.kami.mapper.RedeemRecordMapper;
import com.yuchen.kami.mapper.ShopOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CardKeyMapper cardKeyMapper;
    private final ProductMapper productMapper;
    private final CardBatchMapper cardBatchMapper;
    private final RedeemRecordMapper redeemRecordMapper;
    private final ShopOrderMapper shopOrderMapper;
    private final YuKamiProperties properties;

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

        double usageRate = total > 0 ? used * 100.0 / total : 0;
        var recentRedeems = redeemRecordMapper.selectList(new LambdaQueryWrapper<RedeemRecord>()
                .orderByDesc(RedeemRecord::getCreatedAt)
                .last("LIMIT 10"));

        long todayOrders = shopOrderMapper.selectCount(new LambdaQueryWrapper<ShopOrder>()
                .in(ShopOrder::getStatus, ShopOrder.STATUS_DELIVERED, ShopOrder.STATUS_PAID)
                .ge(ShopOrder::getCreatedAt, startOfDay));

        List<ProductStockAlert> lowStockProducts = findLowStockProducts();

        return DashboardStats.builder()
                .totalCards(total)
                .usedCards(used)
                .unusedCards(unused)
                .todayRedeems(todayRedeems)
                .todayOrders(todayOrders)
                .totalProducts(productMapper.selectCount(new LambdaQueryWrapper<Product>()))
                .totalBatches(cardBatchMapper.selectCount(new LambdaQueryWrapper<CardBatch>()))
                .usageRate(usageRate)
                .recentRedeems(recentRedeems)
                .lowStockProducts(lowStockProducts)
                .stockLowThreshold(properties.getStock().getLowThreshold())
                .build();
    }

    private List<ProductStockAlert> findLowStockProducts() {
        int threshold = properties.getStock().getLowThreshold();
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1));
        List<ProductStockAlert> alerts = new ArrayList<>();
        for (Product product : products) {
            long unused = cardKeyMapper.selectCount(new LambdaQueryWrapper<CardKey>()
                    .eq(CardKey::getProductId, product.getId())
                    .eq(CardKey::getStatus, CardKey.STATUS_UNUSED));
            if (unused <= threshold) {
                alerts.add(ProductStockAlert.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .productCode(product.getCode())
                        .unusedCount(unused)
                        .threshold(threshold)
                        .build());
            }
        }
        alerts.sort(Comparator.comparingLong(ProductStockAlert::getUnusedCount));
        return alerts;
    }
}
