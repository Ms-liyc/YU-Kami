package com.yuchen.kami.crypto;

import com.yuchen.kami.config.YuKamiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 多重加密服务：
 * 1. HMAC-SHA256 - 卡密哈希存储（主密钥 + 胡椒盐）
 * 2. AES-256-GCM - 敏感元数据加密
 * 3. SHA-256 - 校验摘要
 * 4. RSA - API 签名验签
 */
@Service
@RequiredArgsConstructor
public class CryptoService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final YuKamiProperties properties;
    private final AesGcmCipher aesGcmCipher;
    private final RsaSigner rsaSigner;

    public String generatePepper() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public String hashCardKey(String plainKey, String pepper) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            String material = properties.getCrypto().getHmacSecret() + ":" + pepper;
            SecretKey key = new SecretKeySpec(material.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] hash = mac.doFinal(plainKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("卡密哈希失败", e);
        }
    }

    public String computeChecksum(String plainKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 6).toUpperCase();
        } catch (Exception e) {
            throw new IllegalStateException("校验码计算失败", e);
        }
    }

    public boolean verifyChecksum(String plainKey, String checksum) {
        return computeChecksum(plainKey).equalsIgnoreCase(checksum);
    }

    public String encryptMeta(String plainText) {
        return aesGcmCipher.encrypt(plainText, properties.getCrypto().getAesKey());
    }

    public String decryptMeta(String cipherText) {
        return aesGcmCipher.decrypt(cipherText, properties.getCrypto().getAesKey());
    }

    public String sign(String data) {
        return rsaSigner.sign(data);
    }

    public boolean verifySignature(String data, String signature) {
        return rsaSigner.verify(data, signature);
    }

    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 计算失败", e);
        }
    }

    public String generateApiSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
