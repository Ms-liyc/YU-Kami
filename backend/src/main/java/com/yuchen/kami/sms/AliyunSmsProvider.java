package com.yuchen.kami.sms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuchen.kami.common.BusinessException;
import com.yuchen.kami.config.YuKamiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 阿里云短信 dysmsapi，用户自行配置 AccessKey / 签名 / 模板。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunSmsProvider implements SmsProvider {

    private static final String ENDPOINT = "https://dysmsapi.aliyuncs.com/";
    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    private final YuKamiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public String type() {
        return "aliyun";
    }

    @Override
    public boolean isConfigured() {
        YuKamiProperties.Sms sms = properties.getSms();
        return StringUtils.hasText(sms.getAliyunAccessKeyId())
                && StringUtils.hasText(sms.getAliyunAccessKeySecret())
                && StringUtils.hasText(sms.getAliyunSignName())
                && StringUtils.hasText(sms.getAliyunTemplateCode());
    }

    @Override
    public void send(SmsMessage message) {
        if (!isConfigured()) {
            throw new BusinessException("阿里云短信未完整配置，请设置 SMS_ALIYUN_* 环境变量");
        }
        YuKamiProperties.Sms sms = properties.getSms();
        try {
            Map<String, String> params = new TreeMap<>();
            params.put("AccessKeyId", sms.getAliyunAccessKeyId());
            params.put("Action", "SendSms");
            params.put("Format", "JSON");
            params.put("PhoneNumbers", message.getPhone());
            params.put("SignName", sms.getAliyunSignName());
            params.put("SignatureMethod", "HMAC-SHA1");
            params.put("SignatureNonce", UUID.randomUUID().toString());
            params.put("SignatureVersion", "1.0");
            params.put("TemplateCode", sms.getAliyunTemplateCode());
            params.put("TemplateParam", objectMapper.writeValueAsString(
                    message.getTemplateParams() != null && !message.getTemplateParams().isEmpty()
                            ? message.getTemplateParams()
                            : Map.of("content", message.getContent() != null ? message.getContent() : "")));
            params.put("Timestamp", TS_FMT.format(Instant.now()));
            params.put("Version", "2017-05-25");

            String canonicalized = params.entrySet().stream()
                    .map(e -> percentEncode(e.getKey()) + "=" + percentEncode(e.getValue()))
                    .collect(Collectors.joining("&"));
            String stringToSign = "GET&" + percentEncode("/") + "&" + percentEncode(canonicalized);
            String signature = sign(stringToSign, sms.getAliyunAccessKeySecret() + "&");
            params.put("Signature", signature);

            String query = params.entrySet().stream()
                    .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT + "?" + query))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = objectMapper.readTree(response.body());
            if (node.has("Code") && !"OK".equals(node.get("Code").asText())) {
                log.warn("阿里云短信失败: code={}", node.path("Code").asText());
                throw new BusinessException("阿里云短信发送失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("阿里云短信请求异常: {}", e.getClass().getSimpleName());
            throw new BusinessException("阿里云短信发送失败");
        }
    }

    private static String sign(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private static String percentEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
    }
}
