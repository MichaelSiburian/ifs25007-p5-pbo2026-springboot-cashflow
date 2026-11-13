package org.delcom.ifs25007springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hash dan verifikasi password dengan SHA-256 (hex). */
public final class PasswordHasher {
    private PasswordHasher() {
    }

    public static String hash(String raw) {
        return digest("SHA-256", raw);
    }

    public static boolean matches(String raw, String hash) {
        return hash(raw).equals(hash);
    }

    static String digest(String algorithm, String raw) {
        try {
            byte[] bytes = MessageDigest.getInstance(algorithm)
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritma hash tidak tersedia: " + algorithm, e);
        }
    }
}
