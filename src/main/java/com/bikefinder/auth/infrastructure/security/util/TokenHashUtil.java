package com.bikefinder.auth.infrastructure.security.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility para hashing de tokens (SHA-256 determinístico).
 * No usar para passwords (para eso usar BCrypt).
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenHashUtil {

    private static final String ALGORITHM = "SHA-256";

    /**
     * Genera hash SHA-256 de un token (determinístico, para búsqueda en BD)
     */
    public static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear token", e);
        }
    }

    /**
     * Verifica si un raw token coincide con un hash almacenado
     */
    public static boolean matches(String rawToken, String storedHash) {
        return hash(rawToken).equals(storedHash);
    }
}
