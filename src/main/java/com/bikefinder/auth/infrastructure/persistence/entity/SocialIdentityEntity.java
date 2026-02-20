package com.bikefinder.auth.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "social_identities", schema = "auth",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_uid"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialIdentityEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String provider; // GOOGLE, APPLE, FACEBOOK

    @Column(name = "provider_uid", nullable = false)
    private String providerUid;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "profile_data", columnDefinition = "jsonb")
    private String profileData; // JSON almacenado como String

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}
