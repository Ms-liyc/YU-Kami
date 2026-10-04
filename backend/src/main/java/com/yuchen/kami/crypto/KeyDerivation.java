package com.yuchen.kami.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * HKDF-SHA256 密钥派生与常量时间比较。
 */
public final class KeyDerivation {

    private KeyDerivation() {
    }

    public static byte[] hkdfSha256(byte[] ikm, byte[] salt, byte[] info, int length) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            byte[] effectiveSalt = (salt == null || salt.length == 0) ? new byte[32] : salt;
            mac.init(new SecretKeySpec(effectiveSalt, "HmacSHA256"));
            byte[] prk = mac.doFinal(ikm);

            byte[] result = new byte[length];
            byte[] previous = new byte[0];
            int offset = 0;
            byte counter = 1;
            while (offset < length) {
                mac.init(new SecretKeySpec(prk, "HmacSHA256"));
                mac.update(previous);
                if (info != null) {
                    mac.update(info);
                }
                mac.update(counter);
                previous = mac.doFinal();
                int copyLen = Math.min(previous.length, length - offset);
                System.arraycopy(previous, 0, result, offset, copyLen);
                offset += copyLen;
                counter++;
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("HKDF 派生失败", e);
        }
    }

    public static byte[] deriveAesKey(String keyMaterial) {
        return deriveAesLayerKey(keyMaterial, 0);
    }

    public static byte[] deriveAesLayerKey(String keyMaterial, int layer) {
        return hkdfSha256(
                keyMaterial.getBytes(StandardCharsets.UTF_8),
                "yu-kami:aes:salt".getBytes(StandardCharsets.UTF_8),
                ("aes-256-gcm:layer:" + layer).getBytes(StandardCharsets.UTF_8),
                32);
    }

    public static byte[] deriveCardHmacKey(String masterSecret, String pepper) {
        return hkdfSha256(
                masterSecret.getBytes(StandardCharsets.UTF_8),
                pepper.getBytes(StandardCharsets.UTF_8),
                "yu-kami:card-key:v2".getBytes(StandardCharsets.UTF_8),
                32);
    }

    public static byte[] deriveCardHmacKeyRound(String masterSecret, String pepper, int round) {
        return hkdfSha256(
                masterSecret.getBytes(StandardCharsets.UTF_8),
                pepper.getBytes(StandardCharsets.UTF_8),
                ("yu-kami:card-key:v3:round:" + round).getBytes(StandardCharsets.UTF_8),
                32);
    }

    public static byte[] deriveChecksumKeyRound(String masterSecret, int round) {
        return hkdfSha256(
                masterSecret.getBytes(StandardCharsets.UTF_8),
                "yu-kami:checksum:salt".getBytes(StandardCharsets.UTF_8),
                ("yu-kami:checksum:v3:round:" + round).getBytes(StandardCharsets.UTF_8),
                32);
    }

    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
