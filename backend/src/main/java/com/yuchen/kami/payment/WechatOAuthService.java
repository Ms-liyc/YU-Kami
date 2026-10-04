package com.yuchen.kami.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.config.YuKamiProperties;
import com.yuchen.kami.crypto.HmacSigner;
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
    private static final String DEFAULT_REDIRECT = "/shop/orders";

    private final PaymentChannelHelper helper;
    private final YuKamiProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final HmacSigner hmacSigner;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public record OAuthState(Long userId, Long orderId, String redirectPath) {}

    public String buildOAuthUrl(PaymentConfig config, String redirectPath, Long orderId, Long userId) {
        String appId = config.getAppId();
        String appSecret = getAppSecret(config);
        if (appId == null || appId.isBlank()) {
            throw new BusinessException("请配置微信 AppID");
        }
        if (appSecret == null) {
            throw new BusinessException("请配置微信 AppSecret（用于 OAuth 获取 openid）");
        }
        String state = buildState(userId, orderId, redirectPath);
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

    public String buildState(Long userId, Long orderId, String redirectPath) {
        String safeRedirect = sanitizeRedirect(redirectPath);
        String encodedRedirect = URLEncoder.encode(safeRedirect, StandardCharsets.UTF_8);
        String payload = userId + "|" + orderId + "|" + encodedRedirect;
        return payload + "|" + hmacSigner.sign(payload);
    }

    public OAuthState parseState(String state) {
        if (state == null || state.isBlank()) {
            throw new BusinessException("缺少 OAuth state");
        }
        int lastSep = state.lastIndexOf('|');
        if (lastSep <= 0) {
            throw new BusinessException("OAuth state 无效");
        }
        String payload = state.substring(0, lastSep);
        String signature = state.substring(lastSep + 1);
        if (!hmacSigner.verify(payload, signature)) {
            throw new BusinessException("OAuth state 签名校验失败");
        }
        String[] parts = payload.split("\\|", 3);
        if (parts.length != 3) {
            throw new BusinessException("OAuth state 格式错误");
        }
        try {
            Long userId = Long.parseLong(parts[0]);
            Long orderId = Long.parseLong(parts[1]);
            String redirectPath = java.net.URLDecoder.decode(parts[2], StandardCharsets.UTF_8);
            return new OAuthState(userId, orderId, sanitizeRedirect(redirectPath));
        } catch (NumberFormatException e) {
            throw new BusinessException("OAuth state 用户标识无效");
        }
    }

    public String handleCallback(PaymentConfig config, String code, OAuthState parsed) {
        String openid = exchangeOpenId(config, code);
        redisTemplate.opsForValue().set(OPENID_KEY_PREFIX + parsed.userId(), openid, Duration.ofHours(2));
        return parsed.redirectPath();
    }

    public String getStoredOpenId(Long userId) {
        return redisTemplate.opsForValue().get(OPENID_KEY_PREFIX + userId);
    }

    public void clearOpenId(Long userId) {
        redisTemplate.delete(OPENID_KEY_PREFIX + userId);
    }

    static String sanitizeRedirect(String redirectPath) {
        if (redirectPath == null || redirectPath.isBlank()) {
            return DEFAULT_REDIRECT;
        }
        String path = redirectPath.trim();
        if (!path.startsWith("/shop/") || path.contains("://") || path.startsWith("//")) {
            return DEFAULT_REDIRECT;
        }
        return path;
    }

    private String exchangeOpenId(PaymentConfig config, String code) {
        try {
            String appId = config.getAppId();
            String appSecret = getAppSecret(config);
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token"
                    + "?appid=" + URLEncoder.encode(appId, StandardCharsets.UTF_8)
                    + "&secret=" + URLEncoder.encode(appSecret, StandardCharsets.UTF_8)
                    + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                    + "&grant_type=authorization_code";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());
            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
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
            throw new BusinessException("微信 OAuth 失败");
        }
    }

    private String getAppSecret(PaymentConfig config) {
        if (config.getAppSecret() != null && !config.getAppSecret().isBlank()) {
            return config.getAppSecret();
        }
        return helper.getJsonField(config, "appSecret");
    }
}
