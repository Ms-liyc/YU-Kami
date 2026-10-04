package com.yuchen.kami.service;

import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.dto.CaptchaResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private YuKamiProperties properties;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private CaptchaService captchaService;

    @Test
    void generate_shouldReturnCaptchaIdAndImage() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        CaptchaResponse response = captchaService.generate();

        assertNotNull(response.getCaptchaId());
        assertNotNull(response.getImageBase64());
        assertFalse(response.getImageBase64().isBlank());
        verify(valueOperations).set(startsWith("captcha:"), anyString(), any());
    }

    @Test
    void validate_shouldPass_whenCodeMatches() {
        stubCaptchaEnabled();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:abc")).thenReturn("ABCD");

        captchaService.validate("abc", "abcd");

        verify(redisTemplate).delete("captcha:abc");
    }

    @Test
    void validate_shouldThrow_whenCodeMismatch() {
        stubCaptchaEnabled();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("captcha:abc")).thenReturn("ABCD");

        assertThrows(BusinessException.class, () -> captchaService.validate("abc", "WXYZ"));
    }

    @Test
    void validate_shouldSkip_whenDisabled() {
        YuKamiProperties.Captcha captcha = new YuKamiProperties.Captcha();
        captcha.setEnabled(false);
        when(properties.getCaptcha()).thenReturn(captcha);

        captchaService.validate(null, null);

        verifyNoInteractions(redisTemplate);
    }

    private void stubCaptchaEnabled() {
        YuKamiProperties.Captcha captcha = new YuKamiProperties.Captcha();
        captcha.setEnabled(true);
        when(properties.getCaptcha()).thenReturn(captcha);
    }
}
