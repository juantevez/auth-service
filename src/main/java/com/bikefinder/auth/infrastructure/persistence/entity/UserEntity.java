package com.bikefinder.auth.infrastructure.persistence.entity;

import com.bikefinder.auth.domain.valueobject.UserStatus;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "users", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;
    @Column(name = "phone_number", length = 20, unique = true)
    private String phoneNumber;
    @Column(name = "phone_verified")
    private Boolean phoneVerified = false;

    // ===== NUEVOS CAMPOS =====

    @Column(name = "gender", length = 20)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "locality_id")
    private Integer localityId;

    @Column(name = "locality_name", length = 100)
    private String localityName;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Column(name = "province_name", length = 100)
    private String provinceName;

    @Column(name = "country_name", length = 100)
    private String countryName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Version // Esto mapea tu columna "version" int4 para control de concurrencia
    private Integer version;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SocialIdentityEntity> socialIdentities = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CredentialEntity credential;

    // ✅ AGREGAR ESTE MÉTODO (se ejecuta ANTES de INSERT)
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.version == null) {
            this.version = 0;
        }
    }
    public enum Gender {
        MALE,
        FEMALE,
        ALIEN,
        PREFER_NOT_TO_SAY
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
