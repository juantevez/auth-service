package com.bikefinder.auth.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.regex.Pattern;

/**
 * Value Object para números de teléfono con validación E.164
 * Formato: +[código país][número] ej: +5491123456789
 */
@Getter
@EqualsAndHashCode
public class PhoneNumber {

    // Patrón básico para E.164 (se puede ajustar según necesidades)
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    private final String value;

    private PhoneNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El número de teléfono no puede ser vacío");
        }

        // Normalizar: quitar espacios, guiones, paréntesis
        String normalized = value.replaceAll("[\\s\\-\\(\\)]", "");

        if (!E164_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Formato inválido. Use E.164: +[código país][número] ej: +5491123456789"
            );
        }

        this.value = normalized;
    }

    /**
     * Factory method con validación
     */
    public static PhoneNumber of(String phoneNumber) {
        return new PhoneNumber(phoneNumber);
    }

    /**
     * Factory method opcional (sin validar, para carga desde BD)
     */
    public static PhoneNumber ofUnsafe(String phoneNumber) {
        if (phoneNumber == null) return null;
        return new PhoneNumber(phoneNumber);
    }

    /**
     * Verifica si está verificado (para 2FA)
     */
    public boolean isVerified() {
        // Lógica de negocio: podría consultar un estado externo
        return true;
    }

    @Override
    public String toString() {
        return value;
    }
}
