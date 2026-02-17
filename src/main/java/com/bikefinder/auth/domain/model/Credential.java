package com.bikefinder.auth.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Credential {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 30;

    private String passwordHash;
    private int failedLoginAttempts;
    private Instant lockedUntil;

    public Credential(String passwordHash) {
        this.passwordHash = passwordHash;
        this.failedLoginAttempts = 0;
    }

    // Eliminamos la dependencia de Spring Security aquí.
    // La validación del hash se hace en el UseCase comparando este hash.
    public boolean verifyPassword(String inputHash) {
        if (isLocked()) return false;
        // En arquitectura hexagonal estricta, el hashing se hace antes
        // de entrar al dominio, o se compara el hash plano aquí.
        return this.passwordHash.equals(inputHash);
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            this.lockedUntil = Instant.now().plus(LOCK_TIME_MINUTES, ChronoUnit.MINUTES);
        }
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public boolean isLocked() {
        if (lockedUntil == null) return false;
        if (Instant.now().isAfter(lockedUntil)) {
            this.lockedUntil = null;
            this.failedLoginAttempts = 0;
            return false;
        }
        return true;
    }

    // --- GETTERS (Necesarios para MapStruct) ---
    public String getPasswordHash() { return passwordHash; }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }
    //public Instant getLockedUntil() { return lockedUntil; }
    //public int getFailedLoginAttempts() { return failedLoginAttempts; }

}
