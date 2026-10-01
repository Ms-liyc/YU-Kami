package com.yuchen.kami.controller;

import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.common.Result;
import com.yuchen.kami.dto.PromotionRequest;
import com.yuchen.kami.dto.PromotionVO;
import com.yuchen.kami.entity.Promotion;
import com.yuchen.kami.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AdminPromotionController {

    private final PromotionService promotionService;

    @GetMapping("/api/admin/promotions")
    public Result<PageResult<PromotionVO>> pagePromotions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(promotionService.page(page, size, keyword, Promotion.KIND_ACTIVITY));
    }

    @GetMapping("/api/admin/promotions/{id}")
    public Result<PromotionVO> getPromotion(@PathVariable Long id) {
        return Result.ok(promotionService.get(id));
    }

    @PostMapping("/api/admin/promotions")
    public Result<PromotionVO> createPromotion(@Valid @RequestBody PromotionRequest request) {
        request.setKind(Promotion.KIND_ACTIVITY);
        return Result.ok(promotionService.create(request));
    }

    @PutMapping("/api/admin/promotions/{id}")
    public Result<PromotionVO> updatePromotion(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
        request.setKind(Promotion.KIND_ACTIVITY);
        return Result.ok(promotionService.update(id, request));
    }

    @DeleteMapping("/api/admin/promotions/{id}")
    public Result<Void> deletePromotion(@PathVariable Long id) {
        promotionService.delete(id);
        return Result.ok();
    }

    @GetMapping("/api/admin/coupons")
    public Result<PageResult<PromotionVO>> pageCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(promotionService.page(page, size, keyword, Promotion.KIND_COUPON));
    }

    @GetMapping("/api/admin/coupons/{id}")
    public Result<PromotionVO> getCoupon(@PathVariable Long id) {
        return Result.ok(promotionService.get(id));
    }

    @PostMapping("/api/admin/coupons")
    public Result<PromotionVO> createCoupon(@Valid @RequestBody PromotionRequest request) {
        request.setKind(Promotion.KIND_COUPON);
        return Result.ok(promotionService.create(request));
    }

    @PutMapping("/api/admin/coupons/{id}")
    public Result<PromotionVO> updateCoupon(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
        request.setKind(Promotion.KIND_COUPON);
        return Result.ok(promotionService.update(id, request));
    }

    @DeleteMapping("/api/admin/coupons/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        promotionService.delete(id);
        return Result.ok();
    }
}
