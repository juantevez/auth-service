package com.bikefinder.auth.domain.model;

import com.bikefinder.auth.domain.valueobject.Email;
import com.bikefinder.auth.domain.valueobject.PhoneNumber;
import com.bikefinder.auth.domain.valueobject.UserId;
import com.bikefinder.auth.domain.valueobject.UserStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private final UserId id;
    private Email email;
    private UserStatus status;
    private String fullName;
    private PhoneNumber phoneNumber;
    private boolean phoneVerified;

    private String avatarUrl;

    // Entidades hijas dentro del Aggregate
    private Credential credential;
    private final List<SocialIdentity> socialIdentities;

    private boolean emailVerified;
    private Instant createdAt;
    private Instant lastLoginAt;
    private int version;

    // ===== NUEVOS CAMPOS =====
    private Gender gender;
    private LocalDate birthDate;
    private Integer localityId;
    private String localityName;
    private String departmentName;
    private String provinceName;
    private String countryName;

    public enum Gender {
        MALE,
        FEMALE,
        ALIEN,
        PREFER_NOT_TO_SAY
    }

    // Constructor para creación nueva
    private User(UserId id, Email email, String fullName) {
        this.id = id;
        this.email = email;
        this.status = UserStatus.PENDING_VERIFICATION; // O ACTIVE según política
        this.fullName = fullName;
        this.socialIdentities = new ArrayList<>();
        this.createdAt = Instant.now();
        this.version = 0;
        this.emailVerified = false;
    }

    public static User register(UserId id, Email email, String fullName) {
        return new User(id, email, fullName);
    }

    // ✅ MÉTODO PARA RECONSTRUCCIÓN DESDE PERSISTENCIA
    public static User fromPersistence(UUID id, String email, String status, String fullName, String avatarUrl) {
        User user = new User(new UserId(id), new Email(email), fullName);
        user.status = UserStatus.valueOf(status);
        user.emailVerified = false;
        user.avatarUrl = avatarUrl;
        return user;
    }

    /**
     * Agrega/actualiza el número de teléfono
     */
    public void updatePhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.phoneVerified = false;  // Requiere re-verificación
    }

    /**
     * Marca el teléfono como verificado (tras enviar SMS y confirmar código)
     */
    public void verifyPhoneNumber() {
        if (this.phoneNumber == null) {
            throw new IllegalStateException("No hay teléfono para verificar");
        }
        this.phoneVerified = true;
    }

    /**
     * Elimina el teléfono asociado
     */
    public void removePhoneNumber() {
        this.phoneNumber = null;
        this.phoneVerified = false;
    }

    // --- Métodos de Comportamiento (Business Logic) ---

    // ✅ Setter package-private para infraestructura
    public void setCredential(Credential credential) {
        this.credential = credential;
    }
    public void linkSocialIdentity(SocialIdentity identity) {
        // Evitar duplicados de mismo proveedor
        boolean exists = socialIdentities.stream()
                .anyMatch(si -> si.getProvider().equals(identity.getProvider()));

        if (exists) {
            throw new IllegalStateException("Ya existe una identidad vinculada para este proveedor");
        }
        this.socialIdentities.add(identity);
    }

    public void successfulLogin() {
        if (this.status == UserStatus.BANNED) {
            throw new IllegalStateException("Usuario baneado");
        }
        this.lastLoginAt = Instant.now();
        if (this.credential != null) {
            this.credential.resetFailedAttempts();
        }
    }

    public void failedLoginAttempt() {
        if (this.credential != null) {
            this.credential.incrementFailedAttempts();
        }
    }

    /**
     * Actualiza la foto de perfil obtenida desde el proveedor social.
     * Solo actualiza si el nuevo valor no es nulo.
     */
    public void updateAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            throw new IllegalArgumentException("El avatarUrl no puede ser nulo o vacío");
        }
        this.avatarUrl = avatarUrl;
    }

    public void verifyEmail() {
        this.emailVerified = true;
        if (this.status == UserStatus.PENDING_VERIFICATION) {
            this.status = UserStatus.ACTIVE; // al verificar email, activar cuenta
        }
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    // --- Getters (Solo lo necesario para la lectura) ---
    public UserId getId() { return id; }
    public Email getEmail() { return email; }
    public UserStatus getStatus() { return status; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public Credential getCredential() { return credential; }
    public boolean isLocked() {
        return credential != null && credential.isLocked();
    }
    public List<SocialIdentity> getSocialIdentities() { return List.copyOf(socialIdentities); }
    public Instant getLastLoginAt() { return lastLoginAt; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Integer getLocalityId() { return localityId; }
    public void setLocalityId(Integer localityId) { this.localityId = localityId; }

    public String getLocalityName() { return localityName; }
    public void setLocalityName(String localityName) { this.localityName = localityName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    public PhoneNumber getPhoneNumber() { return phoneNumber; }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    public boolean isPhoneVerified() { return phoneVerified; }
}
