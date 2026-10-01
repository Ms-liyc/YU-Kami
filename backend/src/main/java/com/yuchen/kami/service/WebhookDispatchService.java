package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.entity.WebhookConfig;
import com.yuchen.kami.entity.WebhookLog;
import com.yuchen.kami.mapper.WebhookConfigMapper;
import com.yuchen.kami.mapper.WebhookLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookDispatchService {

    private final WebhookConfigMapper webhookConfigMapper;
    private final WebhookLogMapper webhookLogMapper;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Async
    public void dispatch(String event, Map<String, Object> data) {
        List<WebhookConfig> configs = webhookConfigMapper.selectList(
                new LambdaQueryWrapper<WebhookConfig>().eq(WebhookConfig::getStatus, 1));
        for (WebhookConfig config : configs) {
            if (!shouldDispatch(config, event)) continue;
            sendWebhook(config, event, data);
        }
    }

    private boolean shouldDispatch(WebhookConfig config, String event) {
        if (config.getEvents() == null) return false;
        for (String e : config.getEvents().split(",")) {
            if (e.trim().equalsIgnoreCase(event)) return true;
        }
        return false;
    }

    private void sendWebhook(WebhookConfig config, String event, Map<String, Object> data) {
        WebhookLog webhookLog = new WebhookLog();
        webhookLog.setWebhookId(config.getId());
        webhookLog.setEvent(event);
        try {
            Map<String, Object> payload = Map.of(
                    "event", event,
                    "timestamp", Instant.now().toEpochMilli(),
                    "data", data
            );
            String body = objectMapper.writeValueAsString(payload);
            webhookLog.setPayload(body);

            String signature = sign(body, config.getSecret());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getUrl()))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("X-YK-Event", event)
                    .header("X-YK-Signature", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            webhookLog.setStatusCode(response.statusCode());
            webhookLog.setResponse(truncate(response.body(), 500));
            webhookLog.setSuccess(response.statusCode() >= 200 && response.statusCode() < 300 ? 1 : 0);
        } catch (Exception e) {
            log.warn("Webhook 发送失败: {} - {}", config.getUrl(), e.getMessage());
            webhookLog.setSuccess(0);
            webhookLog.setResponse(truncate(e.getMessage(), 500));
        }
        webhookLogMapper.insert(webhookLog);
    }

    private String sign(String body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }
}
