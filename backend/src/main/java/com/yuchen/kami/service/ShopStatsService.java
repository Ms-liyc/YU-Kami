package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.dto.RecentOrderItem;
import com.yuchen.kami.dto.ShopPublicStats;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.entity.ShopUser;
import com.yuchen.kami.mapper.ShopOrderMapper;
import com.yuchen.kami.mapper.ShopUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopStatsService {

    private final ShopOrderMapper shopOrderMapper;
    private final ShopUserMapper shopUserMapper;

    public ShopPublicStats getPublicStats() {
        long delivered = shopOrderMapper.selectCount(new LambdaQueryWrapper<ShopOrder>()
                .eq(ShopOrder::getStatus, ShopOrder.STATUS_DELIVERED));
        long users = shopUserMapper.selectCount(new LambdaQueryWrapper<ShopUser>()
                .eq(ShopUser::getStatus, 1));
        return ShopPublicStats.builder()
                .totalOrders(delivered)
                .totalUsers(users)
                .cardsDelivered(delivered)
                .uptime(99.9)
                .build();
    }

    public List<RecentOrderItem> recentDelivered(int limit) {
        List<ShopOrder> orders = shopOrderMapper.selectList(new LambdaQueryWrapper<ShopOrder>()
                .eq(ShopOrder::getStatus, ShopOrder.STATUS_DELIVERED)
                .orderByDesc(ShopOrder::getDeliveredAt)
                .last("LIMIT " + Math.min(limit, 20)));
        Map<Long, String> users = shopUserMapper.selectList(null).stream()
                .collect(Collectors.toMap(ShopUser::getId, ShopUser::getUsername, (a, b) -> a));
        return orders.stream().map(o -> RecentOrderItem.builder()
                .user(maskUser(users.getOrDefault(o.getUserId(), "用户")))
                .product(o.getProductName())
                .price(o.getAmount())
                .time(relativeTime(o.getDeliveredAt() != null ? o.getDeliveredAt() : o.getCreatedAt()))
                .build()).toList();
    }

    private String maskUser(String username) {
        if (username == null || username.length() < 2) return username + "**";
        return username.charAt(0) + "**";
    }

    private String relativeTime(LocalDateTime time) {
        if (time == null) return "刚刚";
        long minutes = Duration.between(time, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";
        long hours = minutes / 60;
        if (hours < 24) return hours + "小时前";
        return time.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
    }
}
