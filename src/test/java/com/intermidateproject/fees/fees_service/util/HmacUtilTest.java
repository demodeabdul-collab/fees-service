package com.intermidateproject.fees.fees_service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HmacUtilTest {

    private static final String SECRET = "test-secret";
    private static final String PAYLOAD = "{\"invoiceId\":1,\"amount\":200.00,\"reference\":\"REF001\"}";

    @Test
    void computeHmacSha256_matchesKnownTestVector() {
        // Widely published HMAC-SHA256 example, independent of our own code
        String result = HmacUtil.computeHmacSha256(
                "The quick brown fox jumps over the lazy dog", "key");

        assertEquals(
                "f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8",
                result);
    }

    @Test
    void computeHmacSha256_isDeterministic() {
        String first = HmacUtil.computeHmacSha256(PAYLOAD, SECRET);
        String second = HmacUtil.computeHmacSha256(PAYLOAD, SECRET);

        assertEquals(first, second);
    }

    @Test
    void computeHmacSha256_returns64CharLowercaseHex() {
        String signature = HmacUtil.computeHmacSha256(PAYLOAD, SECRET);

        assertEquals(64, signature.length());
        assertTrue(signature.matches("[0-9a-f]+"));
    }

    @Test
    void isValidSignature_returnsTrue_forCorrectSignature() {
        String signature = HmacUtil.computeHmacSha256(PAYLOAD, SECRET);

        assertTrue(HmacUtil.isValidSignature(PAYLOAD, SECRET, signature));
    }

    @Test
    void isValidSignature_returnsFalse_whenPayloadIsTampered() {
        String signature = HmacUtil.computeHmacSha256(PAYLOAD, SECRET);
        String tampered = PAYLOAD.replace("200.00", "2000.00");

        assertFalse(HmacUtil.isValidSignature(tampered, SECRET, signature));
    }

    @Test
    void isValidSignature_returnsFalse_whenSecretIsWrong() {
        String signature = HmacUtil.computeHmacSha256(PAYLOAD, SECRET);

        assertFalse(HmacUtil.isValidSignature(PAYLOAD, "wrong-secret", signature));
    }

    @Test
    void isValidSignature_returnsFalse_whenSignatureIsNull() {
        assertFalse(HmacUtil.isValidSignature(PAYLOAD, SECRET, null));
    }

    @Test
    void isValidSignature_returnsFalse_whenSignatureIsGarbage() {
        assertFalse(HmacUtil.isValidSignature(PAYLOAD, SECRET, "not-a-real-signature"));
    }
}