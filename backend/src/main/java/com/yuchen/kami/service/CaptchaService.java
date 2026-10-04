package com.yuchen.kami.service;

import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.dto.CaptchaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final String REDIS_PREFIX = "captcha:";
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final StringRedisTemplate redisTemplate;
    private final YuKamiProperties properties;
    private final Random random = new Random();

    public CaptchaResponse generate() {
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String code = randomCode(4);
        redisTemplate.opsForValue().set(REDIS_PREFIX + captchaId, code, TTL);
        String imageBase64 = renderImage(code);
        return new CaptchaResponse(captchaId, imageBase64);
    }

    public void validate(String captchaId, String captchaCode) {
        if (!properties.getCaptcha().isEnabled()) {
            return;
        }
        if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            throw new BusinessException("请输入验证码");
        }
        String key = REDIS_PREFIX + captchaId;
        String stored = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        if (stored == null || !stored.equalsIgnoreCase(captchaCode.trim())) {
            throw new BusinessException("验证码错误或已过期");
        }
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private String renderImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
            g.drawString(String.valueOf(code.charAt(i)), 10 + i * 26, 28 + random.nextInt(6) - 3);
        }
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }
        g.dispose();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new BusinessException("验证码生成失败");
        }
    }
}
