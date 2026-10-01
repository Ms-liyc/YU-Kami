package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.CreateOrderRequest;
import com.yuchen.kami.dto.OrderVO;
import com.yuchen.kami.dto.PayOrderRequest;
import com.yuchen.kami.dto.PaymentChannelVO;
import com.yuchen.kami.dto.PrepayResponse;
import com.yuchen.kami.dto.PricingPreviewRequest;
import com.yuchen.kami.dto.PricingResult;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.service.OrderService;
import com.yuchen.kami.service.PaymentService;
import com.yuchen.kami.service.ProductService;
import com.yuchen.kami.service.PromotionService;
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
    private final ProductService productService;
    private final PromotionService promotionService;

    @PostMapping("/pricing/preview")
    public Result<PricingResult> preview(@Valid @RequestBody PricingPreviewRequest request, Authentication auth) {
        Long userId = (Long) auth.getDetails();
        Product product = productService.getById(request.getProductId());
        int qty = request.getQuantity() != null ? request.getQuantity() : 1;
        return Result.ok(promotionService.calculate(product, qty, userId, request.getCouponCode()));
    }

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

    @PostMapping("/prepay")
    public Result<PrepayResponse> prepay(@Valid @RequestBody PayOrderRequest request, Authentication auth) {
        Long userId = (Long) auth.getDetails();
        return Result.ok(paymentService.prepay(userId, request.getOrderId(), request.getPaymentMethod(),
                request.getOpenid(), request.getWechatJsapi()));
    }

    @GetMapping("/{id}/status")
    public Result<OrderVO> status(@PathVariable Long id, Authentication auth) {
        return Result.ok(paymentService.getOrderStatus(id, (Long) auth.getDetails()));
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
    public Result<List<PaymentChannelVO>> channels() {
        return Result.ok(paymentService.availableChannels().stream()
                .map(PaymentChannelVO::from)
                .toList());
    }
}
