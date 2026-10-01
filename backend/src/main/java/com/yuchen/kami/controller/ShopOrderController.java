package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.CreateOrderRequest;
import com.yuchen.kami.dto.OrderVO;
import com.yuchen.kami.dto.PayOrderRequest;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.service.OrderService;
import com.yuchen.kami.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shop/orders")
@RequiredArgsConstructor
public class ShopOrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping
    public Result<ShopOrder> create(@Valid @RequestBody CreateOrderRequest request, Authentication auth) {
        Long userId = (Long) auth.getDetails();
        return Result.ok(orderService.createOrder(userId, request));
    }

    @GetMapping
    public Result<PageResult<OrderVO>> myOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return Result.ok(orderService.userOrders((Long) auth.getDetails(), page, size));
    }

    @PostMapping("/pay")
    public Result<OrderVO> pay(@Valid @RequestBody PayOrderRequest request, Authentication auth) {
        OrderVO vo = paymentService.pay((Long) auth.getDetails(), request.getOrderId(), request.getPaymentMethod());
        return Result.ok(vo);
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, Authentication auth) {
        orderService.cancelOrder(id, (Long) auth.getDetails());
        return Result.ok();
    }

    @GetMapping("/{id}/card")
    public Result<Map<String, String>> getCard(@PathVariable Long id, Authentication auth) {
        String cardKey = paymentService.getDeliveredCardKey(id, (Long) auth.getDetails());
        return Result.ok(Map.of("cardKey", cardKey));
    }

    @GetMapping("/payment-channels")
    public Result<List<PaymentConfig>> channels() {
        return Result.ok(paymentService.availableChannels());
    }
}
