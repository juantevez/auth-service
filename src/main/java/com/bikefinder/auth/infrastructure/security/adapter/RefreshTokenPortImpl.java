package com.bikefinder.auth.infrastructure.security.adapter;

import com.bikefinder.auth.application.port.output.RefreshTokenPort;
import com.bikefinder.auth.domain.valueobject.UserId;
import com.bikefinder.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import com.bikefinder.auth.infrastructure.persistence.entity.UserEntity;
import com.bikefinder.auth.infrastructure.persistence.repository.JpaRefreshTokenRepository;
import com.bikefinder.auth.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenPortImpl implements RefreshTokenPort {

    private final JpaRefreshTokenRepository refreshTokenRepository;
    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String createToken(UserId userId, String familyId) {
        String rawToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        String tokenHash = passwordEncoder.encode(rawToken);

        // ✅ Cargar usuario (si es necesario)
        UserEntity user = userRepository.findById(userId.value())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId.value()));

        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash(tokenHash)
                .familyId(UUID.fromString(familyId))
                .expiresAt(Instant.now().plusSeconds(604800))
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(entity);
        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> validateToken(String rawToken) {
        Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByTokenHash(rawToken);

        if (tokenOpt.isPresent()) {
            RefreshTokenEntity token = tokenOpt.get();
            if (token.getExpiresAt().isAfter(Instant.now()) && token.getRevokedAt() == null) {
                return Optional.of(token.getUser().getId().toString());
            }
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public void revokeToken(String rawToken) {
        refreshTokenRepository.findByTokenHash(rawToken)
                .ifPresent(token -> {
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(UserId userId) {
        refreshTokenRepository.deleteByUserId(userId.value());
    }

    @Override
    @Transactional
    public void revokeFamilyTokens(String familyId) {
        // Implementar según necesites
    }
}
