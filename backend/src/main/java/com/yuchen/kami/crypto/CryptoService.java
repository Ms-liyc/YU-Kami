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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多重加密服务：
 * 1. 多轮 HMAC-SHA256 - 卡密哈希存储（v3 默认 3 轮，兼容 v1/v2）
 * 2. 多层 AES-256-GCM - 元数据与 Redis 缓存（默认 3 层）
 * 3. 多轮 HMAC-SHA256 - 卡密校验码（兼容 v1/v2）
 * 4. RSA - API 签名验签
 */
@Service
@RequiredArgsConstructor
public class CryptoService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CACHE_CIPHER_PREFIX = "enc:";
    private static final Pattern CACHE_MULTI_ROUND_PREFIX = Pattern.compile("^enc(\\d+):(.+)$");
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

    /** 新卡密使用 v3 多轮 HMAC */
    public String hashCardKey(String plainKey, String pepper) {
        return hashCardKeyV3(plainKey, pepper);
    }

    public boolean matchesCardKeyHash(String plainKey, String pepper, String storedHash) {
        return KeyDerivation.constantTimeEquals(hashCardKeyV3(plainKey, pepper), storedHash)
                || KeyDerivation.constantTimeEquals(hashCardKeyV2(plainKey, pepper), storedHash)
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

    /** v2：单轮 HKDF 派生 HMAC 密钥 */
    public String hashCardKeyV2(String plainKey, String pepper) {
        return hmacHex(plainKey.getBytes(StandardCharsets.UTF_8),
                KeyDerivation.deriveCardHmacKey(properties.getCrypto().getHmacSecret(), pepper));
    }

    /** v3：多轮 HKDF 派生密钥链式 HMAC（轮数可配置，默认 3） */
    public String hashCardKeyV3(String plainKey, String pepper) {
        return multiRoundHmacHex(
                plainKey.getBytes(StandardCharsets.UTF_8),
                properties.getCrypto().getHashRounds(),
                round -> KeyDerivation.deriveCardHmacKeyRound(
                        properties.getCrypto().getHmacSecret(), pepper, round));
    }

    /** 新卡密使用 v3 多轮 HMAC 校验码（8 位） */
    public String computeChecksum(String dataPart) {
        return computeChecksumV3(dataPart);
    }

    public boolean verifyChecksum(String dataPart, String checksum) {
        if (checksum == null || checksum.isBlank()) {
            return false;
        }
        String normalized = checksum.trim();
        if (normalized.length() == CHECKSUM_V2_LENGTH) {
            return KeyDerivation.constantTimeEquals(computeChecksumV3(dataPart), normalized)
                    || KeyDerivation.constantTimeEquals(computeChecksumV2(dataPart), normalized);
        }
        if (normalized.length() == CHECKSUM_V1_LENGTH) {
            return KeyDerivation.constantTimeEquals(computeChecksumV1(dataPart), normalized);
        }
        return KeyDerivation.constantTimeEquals(computeChecksumV3(dataPart), normalized)
                || KeyDerivation.constantTimeEquals(computeChecksumV2(dataPart), normalized)
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

    /** v2：单轮 HMAC-SHA256 截断 8 位 */
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

    /** v3：多轮 HMAC-SHA256 链式截断 8 位 */
    public String computeChecksumV3(String dataPart) {
        String hex = multiRoundHmacHex(
                dataPart.getBytes(StandardCharsets.UTF_8),
                properties.getCrypto().getHashRounds(),
                round -> KeyDerivation.deriveChecksumKeyRound(properties.getCrypto().getHmacSecret(), round));
        return hex.substring(0, CHECKSUM_V2_LENGTH).toUpperCase();
    }

    public String encryptMeta(String plainText) {
        return multiLayerEncrypt(plainText, properties.getCrypto().getAesRounds());
    }

    public String decryptMeta(String cipherText) {
        return multiLayerDecrypt(cipherText, properties.getCrypto().getAesRounds());
    }

    /** Redis 临时缓存：多层 AES 加密卡密明文 */
    public String protectForCache(String plainKey) {
        if (plainKey == null) {
            return null;
        }
        int rounds = properties.getCrypto().getAesRounds();
        if (rounds <= 1) {
            return CACHE_CIPHER_PREFIX + aesGcmCipher.encrypt(plainKey, properties.getCrypto().getAesKey());
        }
        return "enc" + rounds + ":" + multiLayerEncrypt(plainKey, rounds);
    }

    /** 读取 Redis 缓存，兼容历史明文与单层 enc: */
    public String unprotectFromCache(String cached) {
        if (cached == null) {
            return null;
        }
        Matcher matcher = CACHE_MULTI_ROUND_PREFIX.matcher(cached);
        if (matcher.matches()) {
            int rounds = Integer.parseInt(matcher.group(1));
            return multiLayerDecrypt(matcher.group(2), rounds);
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

    private String multiLayerEncrypt(String plainText, int rounds) {
        String current = plainText;
        String aesKey = properties.getCrypto().getAesKey();
        for (int layer = 0; layer < rounds; layer++) {
            byte[] layerKey = KeyDerivation.deriveAesLayerKey(aesKey, layer);
            current = aesGcmCipher.encryptWithRawKey(current, layerKey);
        }
        return current;
    }

    private String multiLayerDecrypt(String cipherText, int rounds) {
        String current = cipherText;
        String aesKey = properties.getCrypto().getAesKey();
        for (int layer = rounds - 1; layer >= 0; layer--) {
            byte[] layerKey = KeyDerivation.deriveAesLayerKey(aesKey, layer);
            current = aesGcmCipher.decryptWithRawKey(current, layerKey);
        }
        return current;
    }

    private String multiRoundHmacHex(byte[] input, int rounds, KeySupplier keySupplier) {
        try {
            byte[] state = input;
            Mac mac = Mac.getInstance("HmacSHA256");
            int effectiveRounds = Math.max(1, rounds);
            for (int round = 1; round <= effectiveRounds; round++) {
                mac.init(new SecretKeySpec(keySupplier.derive(round), "HmacSHA256"));
                state = mac.doFinal(state);
            }
            return HexFormat.of().formatHex(state);
        } catch (Exception e) {
            throw new IllegalStateException("多轮 HMAC 计算失败", e);
        }
    }

    private String hmacHex(byte[] input, byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(input));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 计算失败", e);
        }
    }

    @FunctionalInterface
    private interface KeySupplier {
        byte[] derive(int round);
    }
}
