package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.PaymentChannelVO;
import com.yuchen.kami.dto.PrepayResponse;
import com.yuchen.kami.dto.WalletRechargeRequest;
import com.yuchen.kami.dto.WalletTransactionVO;
import com.yuchen.kami.dto.WalletVO;
import com.yuchen.kami.service.PaymentService;
import com.yuchen.kami.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/wallet")
@RequiredArgsConstructor
public class ShopWalletController {

    private final WalletService walletService;
    private final PaymentService paymentService;

    @GetMapping
    public Result<WalletVO> wallet(Authentication auth) {
        return Result.ok(walletService.getWallet((Long) auth.getDetails()));
    }

    @GetMapping("/transactions")
    public Result<PageResult<WalletTransactionVO>> transactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return Result.ok(walletService.listTransactions((Long) auth.getDetails(), page, size));
    }

    @GetMapping("/recharge/channels")
    public Result<List<PaymentChannelVO>> rechargeChannels() {
        return Result.ok(paymentService.availableRechargeChannels());
    }

    @PostMapping("/recharge")
    public Result<PrepayResponse> recharge(@Valid @RequestBody WalletRechargeRequest request,
                                           Authentication auth) {
        Long userId = (Long) auth.getDetails();
        return Result.ok(paymentService.recharge(userId, request.getAmount(), request.getPaymentMethod(),
                request.getOpenid(), request.getWechatJsapi()));
    }
}
