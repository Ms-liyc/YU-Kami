package com.yuchen.kami.crypto;

import com.yuchen.kami.config.YuKamiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class RsaSigner {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public RsaSigner(YuKamiProperties properties) {
        try {
            String privatePath = properties.getCrypto().getRsaPrivateKeyPath();
            String publicPath = properties.getCrypto().getRsaPublicKeyPath();
            if (privatePath != null && !privatePath.isBlank() && publicPath != null && !publicPath.isBlank()) {
                // 生产环境从文件加载
                this.privateKey = null;
                this.publicKey = null;
            } else {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair pair = generator.generateKeyPair();
                this.privateKey = pair.getPrivate();
                this.publicKey = pair.getPublic();
                log.info("RSA 密钥对已自动生成（开发模式），生产环境请配置密钥文件");
            }
        } catch (Exception e) {
            throw new IllegalStateException("RSA 初始化失败", e);
        }
    }

    public String sign(String data) {
        if (privateKey == null) {
            return "";
        }
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new IllegalStateException("RSA 签名失败", e);
        }
    }

    public boolean verify(String data, String signBase64) {
        if (publicKey == null || signBase64 == null || signBase64.isBlank()) {
            return true;
        }
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(signBase64));
        } catch (Exception e) {
            return false;
        }
    }

    public String getPublicKeyBase64() {
        if (publicKey == null) {
            return "";
        }
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
}
