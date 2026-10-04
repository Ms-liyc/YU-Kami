package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.AdminShopUserVO;
import com.yuchen.kami.dto.ShopUserStatusRequest;
import com.yuchen.kami.dto.WalletAdjustRequest;
import com.yuchen.kami.service.AdminShopUserService;
import com.yuchen.kami.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/shop-users")
@RequiredArgsConstructor
public class AdminShopUserController {

    private final AdminShopUserService adminShopUserService;
    private final AuditService auditService;

    @GetMapping
    public Result<PageResult<AdminShopUserVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminShopUserService.page(page, size, keyword));
    }

    @PostMapping("/{id}/wallet/adjust")
    public Result<AdminShopUserVO> adjustWallet(@PathVariable Long id,
                                                @Valid @RequestBody WalletAdjustRequest request,
                                                Authentication auth,
                                                HttpServletRequest httpRequest) {
        AdminShopUserVO vo = adminShopUserService.adjustWallet(id, request.getAmount(), request.getRemark());
        String action = request.getAmount().signum() >= 0 ? "充值" : "扣减";
        auditService.log((Long) auth.getDetails(), auth.getName(), "WALLET_ADJUST", "shop_user",
                action + " ¥" + request.getAmount().abs() + " → " + vo.getUsername()
                        + (request.getRemark() != null ? " (" + request.getRemark() + ")" : ""),
                httpRequest.getRemoteAddr());
        return Result.ok(vo);
    }

    @PostMapping("/{id}/status")
    public Result<AdminShopUserVO> updateStatus(@PathVariable Long id,
                                                @Valid @RequestBody ShopUserStatusRequest request,
                                                Authentication auth,
                                                HttpServletRequest httpRequest) {
        AdminShopUserVO vo = adminShopUserService.updateStatus(id, request.getStatus());
        String action = request.getStatus() == 1 ? "启用" : "禁用";
        auditService.log((Long) auth.getDetails(), auth.getName(), "SHOP_USER_STATUS", "shop_user",
                action + "买家 " + vo.getUsername(), httpRequest.getRemoteAddr());
        return Result.ok(vo);
    }
}
