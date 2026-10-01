package com.yuchen.kami.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.common.PageResult;
import com.yuchen.kami.crypto.CryptoService;
import com.yuchen.kami.dto.WebhookRequest;
import com.yuchen.kami.entity.WebhookConfig;
import com.yuchen.kami.entity.WebhookLog;
import com.yuchen.kami.mapper.WebhookConfigMapper;
import com.yuchen.kami.mapper.WebhookLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookConfigService {

    private final WebhookConfigMapper webhookConfigMapper;
    private final WebhookLogMapper webhookLogMapper;
    private final CryptoService cryptoService;

    public PageResult<WebhookConfig> page(int page, int size) {
        Page<WebhookConfig> result = webhookConfigMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<WebhookConfig>().orderByDesc(WebhookConfig::getCreatedAt));
        result.getRecords().forEach(w -> w.setSecret(maskSecret(w.getSecret())));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    public WebhookConfig create(WebhookRequest request) {
        WebhookConfig config = new WebhookConfig();
        config.setName(request.getName());
        config.setUrl(request.getUrl());
        config.setSecret(request.getSecret() != null && !request.getSecret().isBlank()
                ? request.getSecret() : cryptoService.generateApiSecret());
        config.setEvents(request.getEvents() != null ? request.getEvents() : "REDEEM_SUCCESS");
        config.setStatus(1);
        webhookConfigMapper.insert(config);
        return config;
    }

    public WebhookConfig update(Long id, WebhookRequest request) {
        WebhookConfig config = getById(id);
        config.setName(request.getName());
        config.setUrl(request.getUrl());
        if (request.getSecret() != null && !request.getSecret().isBlank()) {
            config.setSecret(request.getSecret());
        }
        if (request.getEvents() != null) {
            config.setEvents(request.getEvents());
        }
        webhookConfigMapper.updateById(config);
        config.setSecret(maskSecret(config.getSecret()));
        return config;
    }

    public void toggleStatus(Long id) {
        WebhookConfig config = getById(id);
        config.setStatus(config.getStatus() == 1 ? 0 : 1);
        webhookConfigMapper.updateById(config);
    }

    public void delete(Long id) {
        webhookConfigMapper.deleteById(id);
    }

    public PageResult<WebhookLog> logs(int page, int size, Long webhookId) {
        LambdaQueryWrapper<WebhookLog> wrapper = new LambdaQueryWrapper<>();
        if (webhookId != null) wrapper.eq(WebhookLog::getWebhookId, webhookId);
        wrapper.orderByDesc(WebhookLog::getCreatedAt);
        Page<WebhookLog> result = webhookLogMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }

    private WebhookConfig getById(Long id) {
        WebhookConfig config = webhookConfigMapper.selectById(id);
        if (config == null) {
            throw new BusinessException("Webhook 不存在");
        }
        return config;
    }

    private String maskSecret(String secret) {
        if (secret == null || secret.length() < 8) return "********";
        return secret.substring(0, 4) + "****" + secret.substring(secret.length() - 4);
    }
}
