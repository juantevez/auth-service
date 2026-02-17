package com.bikefinder.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class SocialIdentity {
    private UUID id;
    private String provider; // GOOGLE, APPLE...
    private String providerUid;
    private String accessToken; // Encriptado
    private Instant tokenExpiresAt;

    public SocialIdentity(String provider, String providerUid) {
        this.id = UUID.randomUUID();
        this.provider = provider;
        this.providerUid = providerUid;
    }

    public void updateTokens(String accessToken, Instant expiresAt) {
        this.accessToken = accessToken;
        this.tokenExpiresAt = expiresAt;
    }

    public String getProvider() { return provider; }
    public String getProviderUid() { return providerUid; }
    public UUID getId() { return id; }
}
