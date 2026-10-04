package com.yuchen.kami.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.config.YuKamiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通用 HTTP 短信网关：POST JSON 到用户自建接口，可对接任意云厂商或 Serverless。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpSmsProvider implements SmsProvider {

    private final YuKamiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public String type() {
        return "http";
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(properties.getSms().getHttpUrl());
    }

    @Override
    public void send(SmsMessage message) {
        String url = properties.getSms().getHttpUrl();
        if (!StringUtils.hasText(url)) {
            throw new BusinessException("HTTP 短信网关未配置 SMS_HTTP_URL");
        }
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("phone", message.getPhone());
            body.put("message", message.getContent());
            body.put("event", message.getEvent());
            if (message.getTemplateParams() != null && !message.getTemplateParams().isEmpty()) {
                body.put("data", message.getTemplateParams());
            }
            String json = objectMapper.writeValueAsString(body);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
            String secret = properties.getSms().getHttpSecret();
            if (StringUtils.hasText(secret)) {
                builder.header("X-SMS-Secret", secret);
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("HTTP 短信网关返回非 2xx: status={}", response.statusCode());
                throw new BusinessException("短信网关返回错误状态: " + response.statusCode());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("HTTP 短信发送失败: {}", e.getClass().getSimpleName());
            throw new BusinessException("短信网关请求失败");
        }
    }
}
