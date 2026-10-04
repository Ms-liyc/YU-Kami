package com.yuchen.kami.service;

import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.sms.DisabledSmsProvider;
import com.yuchen.kami.sms.HttpSmsProvider;
import com.yuchen.kami.sms.LoggingSmsProvider;
import com.yuchen.kami.sms.AliyunSmsProvider;
import com.yuchen.kami.sms.SmsProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Spy private YuKamiProperties properties = new YuKamiProperties();
    @InjectMocks private DisabledSmsProvider disabledSmsProvider;
    @InjectMocks private LoggingSmsProvider loggingSmsProvider;
    @InjectMocks private HttpSmsProvider httpSmsProvider;
    @InjectMocks private AliyunSmsProvider aliyunSmsProvider;
    private SmsProviderFactory factory;
    private SmsService smsService;

    @BeforeEach
    void setUp() {
        factory = new SmsProviderFactory(properties, disabledSmsProvider, httpSmsProvider,
                loggingSmsProvider, aliyunSmsProvider);
        smsService = new SmsService(properties, factory);
    }

    @Test
    void isConfigured_shouldBeFalse_whenProviderNone() {
        properties.getSms().setProvider("none");
        assertFalse(smsService.isConfigured());
    }

    @Test
    void isConfigured_shouldBeTrue_whenProviderLog() {
        properties.getSms().setProvider("log");
        assertTrue(smsService.isConfigured());
    }
}
