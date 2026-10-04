package com.yuchen.kami.common;

/**
 * 敏感信息脱敏，避免日志或 API 响应泄露邮箱、密钥等。
 */
public final class SensitiveMaskUtils {

    private SensitiveMaskUtils() {}

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        String trimmed = email.trim();
        int at = trimmed.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        String local = trimmed.substring(0, at);
        String domain = trimmed.substring(at);
        if (local.length() <= 2) {
            return "*" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }
        String digits = phone.trim().replaceAll("\\s+", "");
        if (digits.length() <= 4) {
            return "***";
        }
        if (digits.length() <= 7) {
            return digits.substring(0, 2) + "***";
        }
        return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 4);
    }
}
