package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.dto.PricingResult;
import com.yuchen.kami.dto.PromotionRequest;
import com.yuchen.kami.dto.PromotionVO;
import com.yuchen.kami.dto.ShopProductVO;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.entity.Promotion;
import com.yuchen.kami.entity.ShopOrder;
import com.yuchen.kami.mapper.ProductMapper;
import com.yuchen.kami.mapper.PromotionMapper;
import com.yuchen.kami.mapper.ShopOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionMapper promotionMapper;
    private final ProductMapper productMapper;
    private final ShopOrderMapper shopOrderMapper;

    public PageResult<PromotionVO> page(int page, int size, String keyword, String kind) {
        LambdaQueryWrapper<Promotion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Promotion::getKind, kind);
        if (StringUtils.hasText(keyword)) {
            if (Promotion.KIND_COUPON.equals(kind)) {
                wrapper.and(w -> w.like(Promotion::getName, keyword).or().like(Promotion::getCode, keyword));
            } else {
                wrapper.like(Promotion::getName, keyword);
            }
        }
        if (Promotion.KIND_ACTIVITY.equals(kind)) {
            wrapper.orderByDesc(Promotion::getPriority);
        }
        wrapper.orderByDesc(Promotion::getCreatedAt);
        Page<Promotion> result = promotionMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords().stream().map(this::toVO).toList(),
                result.getTotal(), page, size);
    }

    public PromotionVO get(Long id) {
        Promotion promotion = promotionMapper.selectById(id);
        if (promotion == null) {
            throw new BusinessException("记录不存在");
        }
        return toVO(promotion);
    }

    @Transactional
    public PromotionVO create(PromotionRequest request) {
        Promotion promotion = fromRequest(request);
        if (Promotion.KIND_COUPON.equals(promotion.getKind())) {
            promotion.setCode(promotion.getCode().trim().toUpperCase());
            if (promotionMapper.selectCount(new LambdaQueryWrapper<Promotion>()
                    .eq(Promotion::getCode, promotion.getCode())) > 0) {
                throw new BusinessException("优惠券码已存在");
            }
            promotion.setUsedCount(0);
        }
        promotionMapper.insert(promotion);
        return toVO(promotion);
    }

    @Transactional
    public PromotionVO update(Long id, PromotionRequest request) {
        Promotion existing = promotionMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("记录不存在");
        }
        Promotion promotion = fromRequest(request);
        promotion.setId(id);
        if (Promotion.KIND_COUPON.equals(promotion.getKind())) {
            String code = promotion.getCode().trim().toUpperCase();
            if (!code.equals(existing.getCode()) && promotionMapper.selectCount(new LambdaQueryWrapper<Promotion>()
                    .eq(Promotion::getCode, code)) > 0) {
                throw new BusinessException("优惠券码已存在");
            }
            promotion.setCode(code);
            promotion.setUsedCount(existing.getUsedCount());
        }
        promotionMapper.updateById(promotion);
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        promotionMapper.deleteById(id);
    }

    // ---------- 定价 ----------

    public PricingResult calculate(Product product, int quantity, Long userId, String couponCode) {
        return calculate(product, quantity, userId, couponCode, LocalDateTime.now());
    }

    public PricingResult calculate(Product product, int quantity, Long userId, String couponCode, LocalDateTime now) {
        if (quantity < 1) {
            throw new BusinessException("数量至少为1");
        }
        BigDecimal original = product.getValue().multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        PricingResult activityResult = applyBestActivity(product, quantity, original, now);
        PricingResult couponResult = applyCoupon(product, quantity, original, userId, couponCode, now);

        if (couponResult != null && couponResult.getFinalAmount().compareTo(activityResult.getFinalAmount()) < 0) {
            return couponResult;
        }
        return activityResult;
    }

    public ShopProductVO toShopVO(Product product) {
        ShopProductVO vo = new ShopProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setCode(product.getCode());
        vo.setCategory(product.getCategory());
        vo.setDescription(product.getDescription());
        vo.setCardType(product.getCardType());
        vo.setValue(product.getValue());
        vo.setDurationDays(product.getDurationDays());
        vo.setStatus(product.getStatus());

        PricingResult pricing = calculate(product, 1, null, null);
        vo.setSalePrice(pricing.getFinalAmount());
        vo.setDiscountAmount(pricing.getDiscountAmount());
        vo.setPromotionName(pricing.getPromotionName());
        vo.setOnSale(pricing.isOnSale());
        if (pricing.getPromotionId() != null) {
            Promotion p = promotionMapper.selectById(pricing.getPromotionId());
            vo.setHoliday(p != null && p.getIsHoliday() != null && p.getIsHoliday() == 1);
        } else {
            vo.setHoliday(false);
        }
        return vo;
    }

    @Transactional
    public void confirmByOrder(ShopOrder order) {
        if (order.getPromotionId() == null) {
            return;
        }
        Promotion promo = promotionMapper.selectById(order.getPromotionId());
        if (promo == null || !Promotion.KIND_COUPON.equals(promo.getKind())) {
            return;
        }
        promo.setUsedCount((promo.getUsedCount() != null ? promo.getUsedCount() : 0) + 1);
        promotionMapper.updateById(promo);
    }

    // ---------- 内部方法 ----------

    private PricingResult applyBestActivity(Product product, int quantity, BigDecimal original, LocalDateTime now) {
        Optional<Promotion> best = findActiveActivities(now).stream()
                .filter(p -> appliesToProduct(p, product.getId()))
                .filter(p -> meetsMinAmount(p.getMinAmount(), original))
                .findFirst();

        if (best.isEmpty()) {
            return noDiscount(original);
        }

        Promotion promotion = best.get();
        BigDecimal finalAmount = computeDiscount(promotion, product.getValue(), quantity, original);
        BigDecimal discount = original.subtract(finalAmount).max(BigDecimal.ZERO);

        return PricingResult.builder()
                .originalAmount(original)
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .promotionId(promotion.getId())
                .promotionName(promotion.getName())
                .appliedType(Promotion.KIND_ACTIVITY)
                .onSale(discount.compareTo(BigDecimal.ZERO) > 0)
                .build();
    }

    private PricingResult applyCoupon(Product product, int quantity, BigDecimal original,
                                      Long userId, String couponCode, LocalDateTime now) {
        if (!StringUtils.hasText(couponCode) || userId == null) {
            return null;
        }
        Promotion coupon = validateCoupon(couponCode, product.getId(), userId, now);
        if (!meetsMinAmount(coupon.getMinAmount(), original)) {
            throw new BusinessException("未达到优惠券使用门槛");
        }
        BigDecimal finalAmount = computeDiscount(coupon, product.getValue(), quantity, original);
        BigDecimal discount = original.subtract(finalAmount).max(BigDecimal.ZERO);

        return PricingResult.builder()
                .originalAmount(original)
                .discountAmount(discount)
                .finalAmount(finalAmount)
                .promotionId(coupon.getId())
                .couponCode(coupon.getCode())
                .appliedType(Promotion.KIND_COUPON)
                .onSale(discount.compareTo(BigDecimal.ZERO) > 0)
                .build();
    }

    private Promotion validateCoupon(String code, Long productId, Long userId, LocalDateTime now) {
        Promotion coupon = promotionMapper.selectOne(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getKind, Promotion.KIND_COUPON)
                .eq(Promotion::getCode, code.trim().toUpperCase()));
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券无效或已停用");
        }
        if (now.isBefore(coupon.getStartAt()) || now.isAfter(coupon.getEndAt())) {
            throw new BusinessException("优惠券不在有效期内");
        }
        if (!appliesToProduct(coupon, productId)) {
            throw new BusinessException("该优惠券不适用于此商品");
        }
        int reserved = countCouponUsage(coupon.getId(), ShopOrder.STATUS_PENDING);
        int used = coupon.getUsedCount() != null ? coupon.getUsedCount() : 0;
        if (coupon.getUsageLimit() != null && used + reserved >= coupon.getUsageLimit()) {
            throw new BusinessException("优惠券已被领完");
        }
        int perUser = coupon.getPerUserLimit() != null ? coupon.getPerUserLimit() : 1;
        if (countUserCouponUsage(coupon.getId(), userId) >= perUser) {
            throw new BusinessException("您已达到该优惠券使用上限");
        }
        return coupon;
    }

    private int countCouponUsage(Long couponId, String status) {
        Long count = shopOrderMapper.selectCount(new LambdaQueryWrapper<ShopOrder>()
                .eq(ShopOrder::getPromotionId, couponId)
                .eq(ShopOrder::getStatus, status));
        return count != null ? count.intValue() : 0;
    }

    private int countUserCouponUsage(Long couponId, Long userId) {
        Long count = shopOrderMapper.selectCount(new LambdaQueryWrapper<ShopOrder>()
                .eq(ShopOrder::getPromotionId, couponId)
                .eq(ShopOrder::getUserId, userId)
                .in(ShopOrder::getStatus, ShopOrder.STATUS_PENDING, ShopOrder.STATUS_PAID, ShopOrder.STATUS_DELIVERED));
        return count != null ? count.intValue() : 0;
    }

    private List<Promotion> findActiveActivities(LocalDateTime now) {
        return promotionMapper.selectList(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getKind, Promotion.KIND_ACTIVITY)
                .eq(Promotion::getStatus, 1)
                .le(Promotion::getStartAt, now)
                .ge(Promotion::getEndAt, now)
                .orderByDesc(Promotion::getPriority));
    }

    boolean appliesToProduct(Promotion promotion, Long productId) {
        List<Long> ids = parseProductIds(promotion.getProductIds());
        return ids.isEmpty() || ids.contains(productId);
    }

    private PricingResult noDiscount(BigDecimal original) {
        return PricingResult.builder()
                .originalAmount(original)
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(original)
                .appliedType("NONE")
                .onSale(false)
                .build();
    }

    private boolean meetsMinAmount(BigDecimal minAmount, BigDecimal original) {
        return minAmount == null || original.compareTo(minAmount) >= 0;
    }

    static BigDecimal computeDiscount(Promotion rule, BigDecimal unitPrice, int quantity, BigDecimal original) {
        BigDecimal result;
        switch (rule.getType()) {
            case Promotion.TYPE_OVERRIDE_PRICE -> result = rule.getDiscountValue().multiply(BigDecimal.valueOf(quantity));
            case Promotion.TYPE_PERCENT_OFF -> {
                BigDecimal off = original.multiply(rule.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                if (rule.getMaxDiscount() != null && off.compareTo(rule.getMaxDiscount()) > 0) {
                    off = rule.getMaxDiscount();
                }
                result = original.subtract(off);
            }
            case Promotion.TYPE_FIXED_OFF -> result = original.subtract(rule.getDiscountValue());
            default -> result = original;
        }
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            result = BigDecimal.ZERO;
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private Promotion fromRequest(PromotionRequest request) {
        validateRequest(request);
        Promotion promotion = new Promotion();
        promotion.setKind(StringUtils.hasText(request.getKind()) ? request.getKind() : Promotion.KIND_ACTIVITY);
        promotion.setCode(request.getCode());
        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setType(request.getType());
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setMinAmount(request.getMinAmount());
        promotion.setMaxDiscount(request.getMaxDiscount());
        promotion.setStartAt(request.getStartAt());
        promotion.setEndAt(request.getEndAt());
        promotion.setProductIds(joinProductIds(request.getProductIds()));
        promotion.setIsHoliday(Boolean.TRUE.equals(request.getHoliday()) ? 1 : 0);
        promotion.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        promotion.setUsageLimit(request.getUsageLimit());
        promotion.setPerUserLimit(request.getPerUserLimit() != null ? request.getPerUserLimit() : 1);
        promotion.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        if (Promotion.KIND_COUPON.equals(promotion.getKind()) && !StringUtils.hasText(promotion.getCode())) {
            throw new BusinessException("优惠券码不能为空");
        }
        return promotion;
    }

    private void validateRequest(PromotionRequest request) {
        if (request.getEndAt().isBefore(request.getStartAt())) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
        if (Promotion.TYPE_PERCENT_OFF.equals(request.getType())) {
            if (request.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0
                    || request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BusinessException("折扣比例需在 1-100 之间");
            }
        }
    }

    private PromotionVO toVO(Promotion promotion) {
        PromotionVO vo = new PromotionVO();
        vo.setId(promotion.getId());
        vo.setKind(promotion.getKind());
        vo.setCode(promotion.getCode());
        vo.setName(promotion.getName());
        vo.setDescription(promotion.getDescription());
        vo.setType(promotion.getType());
        vo.setTypeLabel(typeLabel(promotion.getType()));
        vo.setDiscountValue(promotion.getDiscountValue());
        vo.setMinAmount(promotion.getMinAmount());
        vo.setMaxDiscount(promotion.getMaxDiscount());
        vo.setStartAt(promotion.getStartAt());
        vo.setEndAt(promotion.getEndAt());
        List<Long> productIds = parseProductIds(promotion.getProductIds());
        vo.setScope(productIds.isEmpty() ? "ALL" : "PRODUCT");
        vo.setHoliday(promotion.getIsHoliday() != null && promotion.getIsHoliday() == 1);
        vo.setPriority(promotion.getPriority());
        vo.setUsageLimit(promotion.getUsageLimit());
        vo.setUsedCount(promotion.getUsedCount());
        vo.setPerUserLimit(promotion.getPerUserLimit());
        vo.setStatus(promotion.getStatus());
        vo.setCreatedAt(promotion.getCreatedAt());
        vo.setProductIds(productIds);
        if (!productIds.isEmpty()) {
            Map<Long, String> names = productMapper.selectBatchIds(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, Product::getName));
            vo.setProductNames(productIds.stream().map(id -> names.getOrDefault(id, "")).toList());
        } else {
            vo.setProductNames(Collections.emptyList());
        }
        return vo;
    }

    static List<Long> parseProductIds(String productIds) {
        if (!StringUtils.hasText(productIds)) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (String part : productIds.split(",")) {
            if (StringUtils.hasText(part)) {
                ids.add(Long.parseLong(part.trim()));
            }
        }
        return ids;
    }

    static String joinProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return null;
        }
        return productIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public static String typeLabel(String type) {
        return switch (type) {
            case Promotion.TYPE_PERCENT_OFF -> "百分比折扣";
            case Promotion.TYPE_FIXED_OFF -> "满减";
            case Promotion.TYPE_OVERRIDE_PRICE -> "特价/节日价";
            default -> type;
        };
    }
}
