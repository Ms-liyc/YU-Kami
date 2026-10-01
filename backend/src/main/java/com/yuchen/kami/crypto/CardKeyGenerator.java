package com.yuchen.kami.crypto;

import com.yuchen.kami.config.YuKamiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class CardKeyGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final YuKamiProperties properties;
    private final CryptoService cryptoService;

    public String generate(String prefix) {
        String charset = properties.getCard().getCharset();
        int length = properties.getCard().getDefaultLength();
        StringBuilder sb = new StringBuilder();
        if (prefix != null && !prefix.isBlank()) {
            sb.append(prefix).append('-');
        }
        int bodyLength = length - sb.length() - 7; // 预留校验码和分隔符
        for (int i = 0; i < bodyLength; i++) {
            sb.append(charset.charAt(RANDOM.nextInt(charset.length())));
        }
        sb.append('-').append(cryptoService.computeChecksum(sb.toString()));
        return sb.toString();
    }
}
