package com.yuchen.kami.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuchen.kami.entity.PaymentConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentChannelHelper {

    private final ObjectMapper objectMapper;

    public String getJsonField(PaymentConfig config, String field) {
        if (config.getConfigJson() == null || config.getConfigJson().isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(config.getConfigJson());
            JsonNode val = node.get(field);
            return val != null && !val.isNull() ? val.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isSandbox(PaymentConfig config) {
        String sandbox = getJsonField(config, "sandbox");
        return "true".equalsIgnoreCase(sandbox);
    }
}
