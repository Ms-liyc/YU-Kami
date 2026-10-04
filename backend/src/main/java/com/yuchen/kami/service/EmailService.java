package com.yuchen.kami.service;

import com.yuchen.kami.config.YuKamiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final YuKamiProperties properties;

    public void sendResetEmail(String to, String resetUrl) {
        send("重置密码", to, "请点击以下链接重置密码（30分钟内有效）：\n" + resetUrl);
    }

    public void sendVerifyEmail(String to, String verifyUrl) {
        send("邮箱验证", to, "请点击以下链接验证邮箱（24小时内有效）：\n" + verifyUrl);
    }

    public void sendLowStockAlert(String to, String productName, String productCode, long unused, int threshold) {
        send("库存告警", to,
                "产品「" + productName + "」(" + productCode + ") 可用卡密仅剩 "
                        + unused + " 张，低于阈值 " + threshold + "。请及时补货。");
    }

    private void send(String subject, String to, String text) {
        if (to == null || to.isBlank()) {
            return;
        }
        String from = properties.getMail().getFrom();
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || from == null || from.isBlank()) {
            log.warn("邮件未配置，跳过发送 [{}] 至 {}", subject, to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("YU-Kami - " + subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("邮件发送失败 [{}] 至 {}: {}", subject, to, e.getMessage());
        }
    }
}
