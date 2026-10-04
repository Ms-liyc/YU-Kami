package com.yuchen.kami.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SensitiveMaskUtilsTest {

    @Test
    void maskEmail_shouldHideMiddlePart() {
        String masked = SensitiveMaskUtils.maskEmail("123456789@qq.com");
        assertEquals("1***9@qq.com", masked);
        assertFalse(masked.contains("123456789"));
    }

    @Test
    void maskPhone_shouldHideMiddleDigits() {
        String masked = SensitiveMaskUtils.maskPhone("13812345678");
        assertEquals("138****5678", masked);
        assertFalse(masked.contains("1234"));
    }
}
