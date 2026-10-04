package com.yuchen.kami.crypto;

import com.yuchen.kami.config.YuKamiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptoServiceTest {

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        YuKamiProperties properties = new YuKamiProperties();
        properties.getCrypto().setHmacSecret("test-hmac-secret-32-chars-min!!");
        properties.getCrypto().setAesKey("test-aes-key-32-characters!!!!!");
        cryptoService = new CryptoService(properties, new AesGcmCipher(), new RsaSigner(properties));
    }

    @Test
    void hashCardKeyV2_shouldDifferFromV1() {
        String pepper = cryptoService.generatePepper();
        String plainKey = "ABCD1234-EFGH5678";
        String v1 = cryptoService.hashCardKeyV1(plainKey, pepper);
        String v2 = cryptoService.hashCardKeyV2(plainKey, pepper);
        assertNotEquals(v1, v2);
    }

    @Test
    void matchesCardKeyHash_shouldSupportLegacyV1() {
        String pepper = cryptoService.generatePepper();
        String plainKey = "TESTCARD-ABCDEF";
        String v1Hash = cryptoService.hashCardKeyV1(plainKey, pepper);
        assertTrue(cryptoService.matchesCardKeyHash(plainKey, pepper, v1Hash));
    }

    @Test
    void matchesCardKeyHash_shouldMatchV2() {
        String pepper = cryptoService.generatePepper();
        String plainKey = "TESTCARD-ABCDEFGH";
        String v2Hash = cryptoService.hashCardKeyV2(plainKey, pepper);
        assertTrue(cryptoService.matchesCardKeyHash(plainKey, pepper, v2Hash));
    }

    @Test
    void verifyChecksum_shouldSupportV1AndV2() {
        String dataPart = "PREFIX-BODY1234";
        String v1 = cryptoService.computeChecksumV1(dataPart);
        String v2 = cryptoService.computeChecksumV2(dataPart);
        assertTrue(cryptoService.verifyChecksum(dataPart, v1));
        assertTrue(cryptoService.verifyChecksum(dataPart, v2));
        assertEquals(CryptoService.CHECKSUM_V1_LENGTH, v1.length());
        assertEquals(CryptoService.CHECKSUM_V2_LENGTH, v2.length());
    }

    @Test
    void protectForCache_shouldRoundTrip() {
        String plain = "ABCD1234-EFGH5678";
        String protectedValue = cryptoService.protectForCache(plain);
        assertTrue(protectedValue.startsWith("enc:"));
        assertEquals(plain, cryptoService.unprotectFromCache(protectedValue));
    }

    @Test
    void unprotectFromCache_shouldReadLegacyPlaintext() {
        String legacy = "LEGACY-PLAIN-KEY";
        assertEquals(legacy, cryptoService.unprotectFromCache(legacy));
    }

    @Test
    void resolveChecksumForStorage_shouldExtractEmbeddedSuffix() {
        String dataPart = "BODY12345678";
        String checksum = cryptoService.computeChecksumV2(dataPart);
        String plainKey = dataPart + "-" + checksum;
        assertEquals(checksum, cryptoService.resolveChecksumForStorage(plainKey));
    }

    @Test
    void constantTimeEquals_shouldRejectNull() {
        assertFalse(KeyDerivation.constantTimeEquals(null, "abc"));
        assertFalse(KeyDerivation.constantTimeEquals("abc", null));
    }
}
