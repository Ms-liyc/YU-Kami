package com.yuchen.kami.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.entity.PaymentConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatOAuthService {

    private static final String OPENID_KEY_PREFIX = "wechat:openid:";

    private final PaymentChannelHelper helper;
    private final YuKamiProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String buildOAuthUrl(PaymentConfig config, String redirectPath, Long orderId, Long userId) {
        String appId = config.getAppId();
        String appSecret = getAppSecret(config);
        if (appId == null || appId.isBlank()) {
            throw new BusinessException("请配置微信 AppID");
        }
        if (appSecret == null) {
            throw new BusinessException("请配置微信 AppSecret（用于 OAuth 获取 openid）");
        }
        String state = userId + "|" + orderId + "|" + URLEncoder.encode(redirectPath, StandardCharsets.UTF_8);
        return UriComponentsBuilder
                .fromUriString("https://open.weixin.qq.com/connect/oauth2/authorize")
                .queryParam("appid", appId)
                .queryParam("redirect_uri", properties.getPayment().getBaseUrl() + "/api/shop/payment/wechat/oauth-callback")
                .queryParam("response_type", "code")
                .queryParam("scope", "snsapi_base")
                .queryParam("state", state)
                .fragment("wechat_redirect")
                .build(true)
                .toUriString();
    }

    public String handleCallback(PaymentConfig config, String code, String state, Long userId) {
        String openid = exchangeOpenId(config, code);
        redisTemplate.opsForValue().set(OPENID_KEY_PREFIX + userId, openid, Duration.ofHours(2));
        String redirectPath = "/shop/orders";
        if (state != null) {
            String[] parts = state.split("\\|", 3);
            if (parts.length == 3) {
                redirectPath = java.net.URLDecoder.decode(parts[2], StandardCharsets.UTF_8);
            }
        }
        return redirectPath;
    }

    public String getStoredOpenId(Long userId) {
        return redisTemplate.opsForValue().get(OPENID_KEY_PREFIX + userId);
    }

    public void clearOpenId(Long userId) {
        redisTemplate.delete(OPENID_KEY_PREFIX + userId);
    }

    private String exchangeOpenId(PaymentConfig config, String code) {
        try {
            String appId = config.getAppId();
            String appSecret = getAppSecret(config);
            String url = String.format(
                    "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                    appId, appSecret, code);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());
            if (node.has("errcode")) {
                throw new BusinessException("微信 OAuth 失败: " + node.path("errmsg").asText());
            }
            String openid = node.path("openid").asText(null);
            if (openid == null || openid.isBlank()) {
                throw new BusinessException("微信 OAuth 未返回 openid");
            }
            return openid;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信 OAuth 交换 openid 失败", e);
            throw new BusinessException("微信 OAuth 失败: " + e.getMessage());
        }
    }

    private String getAppSecret(PaymentConfig config) {
        String secret = helper.getJsonField(config, "appSecret");
        if (secret == null) {
            secret = config.getAppSecret();
        }
        return secret;
    }
}
