package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.OrderRefundRequest;
import com.yuchen.kami.dto.OrderVO;
import com.yuchen.kami.dto.PaymentConfigUpdateRequest;
import com.yuchen.kami.dto.PaymentConfigVO;
import com.yuchen.kami.service.OrderService;
import com.yuchen.kami.service.PaymentService;
import com.yuchen.kami.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final RefundService refundService;

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

    @PostMapping("/{id}/refund")
    public Result<OrderVO> refund(@PathVariable Long id, @RequestBody(required = false) OrderRefundRequest request) {
        return Result.ok(refundService.refund(id, request));
    }

    @GetMapping("/payment-config")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<List<PaymentConfigVO>> paymentConfigs() {
        return Result.ok(paymentService.listConfigViews());
    }

    @PutMapping("/payment-config/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<Void> updatePaymentConfig(@PathVariable Long id, @RequestBody PaymentConfigUpdateRequest config) {
        paymentService.updatePaymentConfig(id, config);
        return Result.ok();
    }
}
