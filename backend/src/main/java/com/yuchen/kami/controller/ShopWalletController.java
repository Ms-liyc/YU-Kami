package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.WalletTransactionVO;
import com.yuchen.kami.dto.WalletVO;
import com.yuchen.kami.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop/wallet")
@RequiredArgsConstructor
public class ShopWalletController {

    private final WalletService walletService;

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
}
