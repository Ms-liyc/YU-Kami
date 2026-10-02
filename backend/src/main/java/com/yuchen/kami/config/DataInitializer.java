package com.yuchen.kami.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.entity.Promotion;
import com.yuchen.kami.entity.ShopUser;
import com.yuchen.kami.entity.SysUser;
import com.yuchen.kami.mapper.PaymentConfigMapper;
import com.yuchen.kami.mapper.ProductMapper;
import com.yuchen.kami.mapper.PromotionMapper;
import com.yuchen.kami.mapper.ShopUserMapper;
import com.yuchen.kami.mapper.SysUserMapper;
import com.yuchen.kami.service.SetupService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final ShopUserMapper shopUserMapper;
    private final ProductMapper productMapper;
    private final PromotionMapper promotionMapper;
    private final PaymentConfigMapper paymentConfigMapper;
    private final PasswordEncoder passwordEncoder;
    private final SetupService setupService;

    @Override
    public void run(String... args) {
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin"));
        if (count == 0) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setRole("SUPER_ADMIN");
            admin.setStatus(1);
            sysUserMapper.insert(admin);
            log.info("已创建默认管理员账号: admin / admin123");
        }
        initProduct("月度会员卡", "VIP_MONTH", "DURATION", "29.90", 30,
                "30 天全功能时长权益，付款后秒发卡密");
        initProduct("季度授权码", "VIP_QUARTER", "DURATION", "99.00", 90,
                "90 天授权，适合中期项目与团队试用");
        initProduct("年度旗舰版", "VIP_YEAR", "DURATION", "199.00", 365,
                "365 天不限次使用，性价比之选");
        initProduct("通用余额卡", "BALANCE_100", "BALANCE", "100.00", null,
                "充值型余额卡，可用于平台内消费抵扣");
        initDemoShopUser();
        initDemoPromotions();
        initPaymentChannel("MOCK", "模拟支付", true);
        initPaymentChannel("ALIPAY", "支付宝", false);
        initPaymentChannel("WECHAT", "微信支付", false);
        logPaymentSetupHint();
    }

    private void logPaymentSetupHint() {
        if (setupService.getStatus().isNeedsPaymentSetup()) {
            log.info("============================================================");
            log.info("支付对接提示：当前默认仅启用模拟支付（MOCK），可直接体验购买流程。");
            log.info("如需接入支付宝/微信，请登录管理后台 → 订单管理 → 支付配置，自行填写密钥与回调地址。");
            log.info("配置说明见项目文档: docs/PAYMENT.md");
            log.info("============================================================");
        }
    }

    private void initPaymentChannel(String channel, String desc, boolean enabled) {
        Long count = paymentConfigMapper.selectCount(new LambdaQueryWrapper<PaymentConfig>()
                .eq(PaymentConfig::getChannel, channel));
        if (count == 0) {
            PaymentConfig config = new PaymentConfig();
            config.setChannel(channel);
            config.setStatus(enabled ? 1 : 0);
            config.setConfigJson("{\"description\":\"" + desc + "\",\"setupRequired\":"
                    + (!enabled) + ",\"hint\":\"请自行在管理后台配置密钥并启用\"}");
            paymentConfigMapper.insert(config);
        }
    }

    private void initProduct(String name, String code, String type, String value, Integer days, String desc) {
        Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCode, code));
        if (count == 0) {
            Product p = new Product();
            p.setName(name);
            p.setCode(code);
            p.setCardType(type);
            p.setValue(new BigDecimal(value));
            p.setDurationDays(days);
            p.setStatus(1);
            p.setDescription(desc);
            productMapper.insert(p);
            log.info("已创建演示商品: {} (¥{})", name, value);
        }
    }

    private void initDemoShopUser() {
        Long count = shopUserMapper.selectCount(new LambdaQueryWrapper<ShopUser>()
                .eq(ShopUser::getUsername, "demo"));
        if (count == 0) {
            ShopUser user = new ShopUser();
            user.setUsername("demo");
            user.setPassword(passwordEncoder.encode("demo123"));
            user.setNickname("演示买家");
            user.setEmail("demo@yu-kami.com");
            user.setStatus(1);
            shopUserMapper.insert(user);
            log.info("已创建演示商城账号: demo / demo123");
        }
    }

    private void initDemoPromotions() {
        Product quarter = productMapper.selectOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getCode, "VIP_QUARTER"));
        if (quarter == null) return;

        Long activityCount = promotionMapper.selectCount(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getKind, Promotion.KIND_ACTIVITY)
                .eq(Promotion::getName, "春季限时特惠"));
        if (activityCount == 0) {
            Promotion activity = new Promotion();
            activity.setKind(Promotion.KIND_ACTIVITY);
            activity.setName("春季限时特惠");
            activity.setDescription("季度授权码限时特价");
            activity.setType(Promotion.TYPE_OVERRIDE_PRICE);
            activity.setDiscountValue(new BigDecimal("79.00"));
            activity.setStartAt(LocalDateTime.now().minusDays(1));
            activity.setEndAt(LocalDateTime.now().plusMonths(3));
            activity.setProductIds(String.valueOf(quarter.getId()));
            activity.setPriority(10);
            activity.setStatus(1);
            activity.setUsedCount(0);
            activity.setPerUserLimit(1);
            promotionMapper.insert(activity);
            log.info("已创建演示促销活动: 春季限时特惠（季度卡 ¥79）");
        }

        Long couponCount = promotionMapper.selectCount(new LambdaQueryWrapper<Promotion>()
                .eq(Promotion::getKind, Promotion.KIND_COUPON)
                .eq(Promotion::getCode, "SPRING2026"));
        if (couponCount == 0) {
            Promotion coupon = new Promotion();
            coupon.setKind(Promotion.KIND_COUPON);
            coupon.setCode("SPRING2026");
            coupon.setName("新春优惠券");
            coupon.setDescription("满 50 减 10");
            coupon.setType(Promotion.TYPE_FIXED_OFF);
            coupon.setDiscountValue(new BigDecimal("10.00"));
            coupon.setMinAmount(new BigDecimal("50.00"));
            coupon.setStartAt(LocalDateTime.now().minusDays(1));
            coupon.setEndAt(LocalDateTime.now().plusMonths(6));
            coupon.setUsageLimit(9999);
            coupon.setPriority(5);
            coupon.setStatus(1);
            coupon.setUsedCount(0);
            coupon.setPerUserLimit(3);
            promotionMapper.insert(coupon);
            log.info("已创建演示优惠券: SPRING2026（满50减10）");
        }
    }
}
