package com.yuchen.kami.service;

import com.yuchen.kami.dto.PricingResult;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.entity.Promotion;
import com.yuchen.kami.mapper.ProductMapper;
import com.yuchen.kami.mapper.PromotionMapper;
import com.yuchen.kami.mapper.ShopOrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock private PromotionMapper promotionMapper;
    @Mock private ProductMapper productMapper;
    @Mock private ShopOrderMapper shopOrderMapper;

    @InjectMocks private PromotionService promotionService;

    @Test
    void calculate_shouldApplyPercentActivity() {
        Product product = product(100);
        Promotion promo = activity(Promotion.TYPE_PERCENT_OFF, BigDecimal.valueOf(20));
        when(promotionMapper.selectList(any())).thenReturn(List.of(promo));

        PricingResult result = promotionService.calculate(product, 1, null, null);

        assertEquals(BigDecimal.valueOf(80).setScale(2), result.getFinalAmount());
        assertEquals(Promotion.KIND_ACTIVITY, result.getAppliedType());
    }

    @Test
    void computeDiscount_fixedOff() {
        Promotion rule = activity(Promotion.TYPE_FIXED_OFF, BigDecimal.TEN);
        BigDecimal result = PromotionService.computeDiscount(rule, BigDecimal.valueOf(50), 2, BigDecimal.valueOf(100));
        assertEquals(BigDecimal.valueOf(90).setScale(2), result);
    }

    private Product product(double value) {
        Product p = new Product();
        p.setId(1L);
        p.setValue(BigDecimal.valueOf(value));
        return p;
    }

    private Promotion activity(String type, BigDecimal value) {
        Promotion p = new Promotion();
        p.setId(1L);
        p.setKind(Promotion.KIND_ACTIVITY);
        p.setName("测试活动");
        p.setType(type);
        p.setDiscountValue(value);
        p.setStartAt(LocalDateTime.now().minusDays(1));
        p.setEndAt(LocalDateTime.now().plusDays(1));
        return p;
    }
}
