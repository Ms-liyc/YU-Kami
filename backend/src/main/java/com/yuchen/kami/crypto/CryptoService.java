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
 * 1. HMAC-SHA256 - 卡密哈希存储（HKDF 派生密钥 + 独立胡椒盐，兼容 v1）
 * 2. AES-256-GCM - 敏感元数据与 Redis 临时缓存加密
 * 3. HMAC-SHA256 - 卡密校验码（兼容 v1 SHA-256）
 * 4. RSA - API 签名验签
 */
@Service
@RequiredArgsConstructor
public class CryptoService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CACHE_CIPHER_PREFIX = "enc:";
    public static final int CHECKSUM_V2_LENGTH = 8;
    public static final int CHECKSUM_V1_LENGTH = 6;

    private final YuKamiProperties properties;
    private final AesGcmCipher aesGcmCipher;
    private final RsaSigner rsaSigner;

    public String generatePepper() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /** 新卡密使用 v2 HKDF 派生 HMAC 密钥 */
    public String hashCardKey(String plainKey, String pepper) {
        return hashCardKeyV2(plainKey, pepper);
    }

    public boolean matchesCardKeyHash(String plainKey, String pepper, String storedHash) {
        return KeyDerivation.constantTimeEquals(hashCardKeyV2(plainKey, pepper), storedHash)
                || KeyDerivation.constantTimeEquals(hashCardKeyV1(plainKey, pepper), storedHash);
    }

    /** v1：主密钥与胡椒盐字符串拼接（历史兼容） */
    public String hashCardKeyV1(String plainKey, String pepper) {
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

    /** v2：HKDF 派生每卡独立 HMAC 密钥 */
    public String hashCardKeyV2(String plainKey, String pepper) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            byte[] derived = KeyDerivation.deriveCardHmacKey(properties.getCrypto().getHmacSecret(), pepper);
            mac.init(new SecretKeySpec(derived, "HmacSHA256"));
            byte[] hash = mac.doFinal(plainKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("卡密哈希失败", e);
        }
    }

    /** 新卡密使用 v2 HMAC 校验码（8 位） */
    public String computeChecksum(String dataPart) {
        return computeChecksumV2(dataPart);
    }

    public boolean verifyChecksum(String dataPart, String checksum) {
        if (checksum == null || checksum.isBlank()) {
            return false;
        }
        String normalized = checksum.trim();
        if (normalized.length() == CHECKSUM_V2_LENGTH) {
            return KeyDerivation.constantTimeEquals(computeChecksumV2(dataPart), normalized);
        }
        if (normalized.length() == CHECKSUM_V1_LENGTH) {
            return KeyDerivation.constantTimeEquals(computeChecksumV1(dataPart), normalized);
        }
        return KeyDerivation.constantTimeEquals(computeChecksumV2(dataPart), normalized)
                || KeyDerivation.constantTimeEquals(computeChecksumV1(dataPart), normalized);
    }

    /** v1：SHA-256 截断 6 位（历史兼容） */
    public String computeChecksumV1(String dataPart) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataPart.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, CHECKSUM_V1_LENGTH).toUpperCase();
        } catch (Exception e) {
            throw new IllegalStateException("校验码计算失败", e);
        }
    }

    /** v2：HMAC-SHA256 截断 8 位，需主密钥才能伪造 */
    public String computeChecksumV2(String dataPart) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    properties.getCrypto().getHmacSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(dataPart.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, CHECKSUM_V2_LENGTH).toUpperCase();
        } catch (Exception e) {
            throw new IllegalStateException("校验码计算失败", e);
        }
    }

    public String encryptMeta(String plainText) {
        return aesGcmCipher.encrypt(plainText, properties.getCrypto().getAesKey());
    }

    public String decryptMeta(String cipherText) {
        return aesGcmCipher.decrypt(cipherText, properties.getCrypto().getAesKey());
    }

    /** Redis 临时缓存：AES 加密卡密明文 */
    public String protectForCache(String plainKey) {
        if (plainKey == null) {
            return null;
        }
        return CACHE_CIPHER_PREFIX + aesGcmCipher.encrypt(plainKey, properties.getCrypto().getAesKey());
    }

    /** 读取 Redis 缓存，兼容历史明文条目 */
    public String unprotectFromCache(String cached) {
        if (cached == null) {
            return null;
        }
        if (cached.startsWith(CACHE_CIPHER_PREFIX)) {
            return aesGcmCipher.decrypt(cached.substring(CACHE_CIPHER_PREFIX.length()),
                    properties.getCrypto().getAesKey());
        }
        return cached;
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

    /** 从卡密字符串提取或计算校验码，用于入库索引 */
    public String resolveChecksumForStorage(String plainKey) {
        if (plainKey.contains("-")) {
            int lastDash = plainKey.lastIndexOf('-');
            String suffix = plainKey.substring(lastDash + 1);
            if (suffix.length() == CHECKSUM_V2_LENGTH || suffix.length() == CHECKSUM_V1_LENGTH) {
                return suffix.toUpperCase();
            }
            return computeChecksum(plainKey.substring(0, lastDash));
        }
        return computeChecksum(plainKey);
    }
}
