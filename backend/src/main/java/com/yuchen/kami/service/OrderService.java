package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.dto.CreateOrderRequest;
import com.yuchen.kami.dto.OrderVO;
import com.yuchen.kami.dto.PricingResult;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.entity.Promotion;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.entity.ShopUser;
import com.yuchen.kami.mapper.PromotionMapper;
import com.yuchen.kami.mapper.ShopOrderMapper;
import com.yuchen.kami.mapper.ShopUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ShopOrderMapper shopOrderMapper;
    private final ShopUserMapper shopUserMapper;
    private final ProductService productService;
    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper;

    @Transactional
    public ShopOrder createOrder(Long userId, CreateOrderRequest request) {
        Product product = productService.getById(request.getProductId());
        if (product.getStatus() != 1) {
            throw new BusinessException("产品已下架");
        }
        int qty = request.getQuantity() != null ? request.getQuantity() : 1;
        PricingResult pricing = promotionService.calculate(product, qty, userId, request.getCouponCode());

        ShopOrder order = new ShopOrder();
        order.setOrderNo("O" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000)));
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setOriginalAmount(pricing.getOriginalAmount());
        order.setDiscountAmount(pricing.getDiscountAmount());
        order.setAmount(pricing.getFinalAmount());
        order.setPromotionId(pricing.getPromotionId());
        order.setCouponCode(pricing.getCouponCode());
        order.setQuantity(qty);
        order.setStatus(ShopOrder.STATUS_PENDING);
        shopOrderMapper.insert(order);
        return order;
    }

    public PageResult<OrderVO> userOrders(Long userId, int page, int size, String status, String orderNo) {
        LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<ShopOrder>()
                .eq(ShopOrder::getUserId, userId);
        if (status != null && !status.isBlank()) {
            wrapper.eq(ShopOrder::getStatus, status);
        }
        if (orderNo != null && !orderNo.isBlank()) {
            wrapper.like(ShopOrder::getOrderNo, orderNo.trim());
        }
        wrapper.orderByDesc(ShopOrder::getCreatedAt);
        Page<ShopOrder> result = shopOrderMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords().stream().map(this::toVO).toList(),
                result.getTotal(), page, size);
    }

    public OrderVO findUserOrderByNo(Long userId, String orderNo) {
        ShopOrder order = shopOrderMapper.selectOne(new LambdaQueryWrapper<ShopOrder>()
                .eq(ShopOrder::getUserId, userId)
                .eq(ShopOrder::getOrderNo, orderNo.trim()));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return toVO(order);
    }

    public PageResult<OrderVO> adminOrders(int page, int size, String status) {
        LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(ShopOrder::getStatus, status);
        }
        wrapper.orderByDesc(ShopOrder::getCreatedAt);
        Page<ShopOrder> result = shopOrderMapper.selectPage(new Page<>(page, size), wrapper);
        Map<Long, String> userMap = shopUserMapper.selectList(null).stream()
                .collect(Collectors.toMap(ShopUser::getId, ShopUser::getUsername));
        List<OrderVO> vos = result.getRecords().stream().map(o -> {
            OrderVO vo = toVO(o);
            vo.setUsername(userMap.getOrDefault(o.getUserId(), ""));
            return vo;
        }).toList();
        return new PageResult<>(vos, result.getTotal(), page, size);
    }

    public ShopOrder getOrder(Long orderId, Long userId) {
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该订单");
        }
        return order;
    }

    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        ShopOrder order = getOrder(orderId, userId);
        if (!ShopOrder.STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException("只能取消待支付订单");
        }
        order.setStatus(ShopOrder.STATUS_CANCELLED);
        shopOrderMapper.updateById(order);
    }

    @Transactional
    public void adminCancel(Long orderId) {
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (ShopOrder.STATUS_DELIVERED.equals(order.getStatus())) {
            throw new BusinessException("已发货订单无法取消");
        }
        order.setStatus(ShopOrder.STATUS_CANCELLED);
        shopOrderMapper.updateById(order);
    }

    public OrderVO toVO(ShopOrder order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setProductId(order.getProductId());
        vo.setProductName(order.getProductName());
        vo.setAmount(order.getAmount());
        vo.setOriginalAmount(order.getOriginalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setCouponCode(order.getCouponCode());
        if (order.getPromotionId() != null) {
            Promotion promotion = promotionMapper.selectById(order.getPromotionId());
            if (promotion != null) {
                vo.setPromotionName(promotion.getName());
            }
        }
        vo.setQuantity(order.getQuantity());
        vo.setStatus(order.getStatus());
        vo.setStatusLabel(statusLabel(order.getStatus()));
        vo.setPaymentMethod(order.getPaymentMethod());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setPaidAt(order.getPaidAt());
        vo.setDeliveredAt(order.getDeliveredAt());
        return vo;
    }

    private String statusLabel(String status) {
        return switch (status) {
            case ShopOrder.STATUS_PENDING -> "待支付";
            case ShopOrder.STATUS_PAID -> "已支付";
            case ShopOrder.STATUS_DELIVERED -> "已发货";
            case ShopOrder.STATUS_CANCELLED -> "已取消";
            default -> status;
        };
    }
}
