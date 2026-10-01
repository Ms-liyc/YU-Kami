package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.OrderVO;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.service.OrderService;
import com.yuchen.kami.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @GetMapping
    public Result<PageResult<OrderVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return Result.ok(orderService.adminOrders(page, size, status));
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.adminCancel(id);
        return Result.ok();
    }

    @GetMapping("/payment-config")
    public Result<List<PaymentConfig>> paymentConfigs() {
        return Result.ok(paymentService.listConfigs());
    }

    @PutMapping("/payment-config/{id}")
    public Result<Void> updatePaymentConfig(@PathVariable Long id, @RequestBody PaymentConfig config) {
        config.setId(id);
        paymentService.updatePaymentConfig(config);
        return Result.ok();
    }
}
