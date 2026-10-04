package com.yuchen.kami.service;

import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.entity.SysUser;
import com.yuchen.kami.mapper.ShopUserMapper;
import com.yuchen.kami.mapper.SysUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private SysUserMapper sysUserMapper;
    @Mock private ShopUserMapper shopUserMapper;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private YuKamiProperties properties;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private PasswordResetService passwordResetService;

    @Test
    void requestAdminReset_shouldUseFrontendResetPath() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin@example.com");
        when(sysUserMapper.selectOne(any())).thenReturn(user);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        YuKamiProperties.Mail mail = new YuKamiProperties.Mail();
        mail.setAppUrl("https://shop.example.com");
        when(properties.getMail()).thenReturn(mail);

        passwordResetService.requestAdminReset("admin");

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendResetEmail(eq("admin@example.com"), urlCaptor.capture());
        assertTrue(urlCaptor.getValue().contains("/reset-password?token="));
        assertTrue(urlCaptor.getValue().startsWith("https://shop.example.com"));
    }
}
