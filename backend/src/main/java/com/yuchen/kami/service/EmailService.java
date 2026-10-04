package com.yuchen.kami.service;

import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.SensitiveMaskUtils;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.dto.MailStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final YuKamiProperties properties;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public boolean isConfigured() {
        return mailSenderProvider.getIfAvailable() != null
                && StringUtils.hasText(properties.getMail().getFrom());
    }

    public MailStatusDTO getStatus() {
        return MailStatusDTO.builder()
                .configured(isConfigured())
                .qqMail(mailHost != null && mailHost.contains("qq.com"))
                .hasStockAlertRecipient(hasStockAlertRecipient())
                .build();
    }

    public void sendTestEmail(String to) {
        if (!isConfigured()) {
            throw new BusinessException("邮件服务未配置，请联系管理员在服务器环境变量中配置 SMTP");
        }
        send("邮件测试", to,
                "这是一封来自 YU-Kami 的测试邮件。\n\n"
                        + "若你能收到此邮件，说明 QQ 邮箱 SMTP 配置正确，库存告警等通知将正常发送。",
                true);
        log.info("测试邮件已发送至 {}", SensitiveMaskUtils.maskEmail(to));
    }

    public void sendResetEmail(String to, String resetUrl) {
        send("重置密码", to, "请点击以下链接重置密码（30分钟内有效）：\n" + resetUrl, false);
    }

    public void sendVerifyEmail(String to, String verifyUrl) {
        send("邮箱验证", to, "请点击以下链接验证邮箱（24小时内有效）：\n" + verifyUrl, false);
    }

    public void sendLowStockAlert(String to, String productName, String productCode, long unused, int threshold) {
        send("库存告警", to,
                "产品「" + productName + "」(" + productCode + ") 可用卡密仅剩 "
                        + unused + " 张，低于阈值 " + threshold + "。请及时补货。",
                false);
    }

    private boolean hasStockAlertRecipient() {
        String recipients = properties.getStock().getAlertEmail();
        return recipients != null && !recipients.isBlank();
    }

    private void send(String subject, String to, String text, boolean throwOnError) {
        if (!StringUtils.hasText(to)) {
            return;
        }
        String from = properties.getMail().getFrom();
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || !StringUtils.hasText(from)) {
            log.warn("邮件未配置，跳过发送 [{}]", subject);
            if (throwOnError) {
                throw new BusinessException("邮件服务未配置");
            }
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("YU-Kami - " + subject);
            message.setText(text);
            mailSender.send(message);
            log.info("邮件已发送 [{}] 至 {}", subject, SensitiveMaskUtils.maskEmail(to));
        } catch (Exception e) {
            log.warn("邮件发送失败 [{}] 至 {}: {}", subject, SensitiveMaskUtils.maskEmail(to), e.getClass().getSimpleName());
            if (throwOnError) {
                throw new BusinessException("邮件发送失败，请检查 SMTP 配置与授权码是否正确");
            }
        }
    }
}
