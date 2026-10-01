package com.yuchen.kami.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuchen.kami.entity.PaymentConfig;
import com.yuchen.kami.entity.Product;
import com.yuchen.kami.entity.SysUser;
import com.yuchen.kami.mapper.PaymentConfigMapper;
import com.yuchen.kami.mapper.ProductMapper;
import com.yuchen.kami.mapper.SysUserMapper;

import java.math.BigDecimal;
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
    private final ProductMapper productMapper;
    private final PaymentConfigMapper paymentConfigMapper;
    private final PasswordEncoder passwordEncoder;

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
        initProduct("月度会员", "VIP_MONTH", "DURATION", "29.90", 30);
        initProduct("年度会员", "VIP_YEAR", "DURATION", "299.00", 365);
        initProduct("通用余额卡", "BALANCE_100", "BALANCE", "100.00", null);
        initPaymentChannel("MOCK", "模拟支付");
        initPaymentChannel("ALIPAY", "支付宝");
        initPaymentChannel("WECHAT", "微信支付");
    }

    private void initPaymentChannel(String channel, String desc) {
        Long count = paymentConfigMapper.selectCount(new LambdaQueryWrapper<PaymentConfig>()
                .eq(PaymentConfig::getChannel, channel));
        if (count == 0) {
            PaymentConfig config = new PaymentConfig();
            config.setChannel(channel);
            config.setStatus("MOCK".equals(channel) ? 1 : 0);
            config.setConfigJson("{\"description\":\"" + desc + "\"}");
            paymentConfigMapper.insert(config);
        }
    }

    private void initProduct(String name, String code, String type, String value, Integer days) {
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
            p.setDescription(name + " 权益");
            productMapper.insert(p);
        }
    }
}
