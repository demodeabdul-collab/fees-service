package com.intermidateproject.fees.fees_service.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HmacUtil {

    private static final String ALGORITHM = "HmacSHA256";

    private HmacUtil() {
        // utility class, no instances
    }

    public static String computeHmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(key);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC signature", e);
        }
    }

    public static boolean isValidSignature(String payload, String secret, String receivedSignature) {
        if (receivedSignature == null) {
            return false;
        }
        String expected = computeHmacSha256(payload, secret);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8));
    }
}