package com.bikefinder.auth.domain.model;

import com.bikefinder.auth.domain.valueobject.UserId;
import java.time.Instant;
import java.util.UUID;

public class VerificationToken {

    private final UUID id;
    private final UserId userId;
    private final String token;
    private final VerificationTokenType type;
    private final Instant expiresAt;
    private Instant usedAt;
    private final Instant createdAt;

    private VerificationToken(UUID id, UserId userId, String token,
                              VerificationTokenType type, Instant expiresAt, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.type = type;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public static VerificationToken create(UserId userId, VerificationTokenType type, int expirationMinutes) {
        return new VerificationToken(
                UUID.randomUUID(),
                userId,
                UUID.randomUUID().toString().replace("-", ""), // token simple y único
                type,
                Instant.now().plusSeconds(expirationMinutes * 60L),
                Instant.now()
        );
    }

    public static VerificationToken fromPersistence(UUID id, UserId userId, String token,
                                                    VerificationTokenType type, Instant expiresAt,
                                                    Instant usedAt, Instant createdAt) {
        VerificationToken vt = new VerificationToken(id, userId, token, type, expiresAt, createdAt);
        vt.usedAt = usedAt;
        return vt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markAsUsed() {
        this.usedAt = Instant.now();
    }

    // Getters
    public UUID getId() { return id; }
    public UserId getUserId() { return userId; }
    public String getToken() { return token; }
    public VerificationTokenType getType() { return type; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
