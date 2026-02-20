package com.bikefinder.auth.infrastructure.security.adapter;

import com.bikefinder.auth.application.port.output.RefreshTokenPort;
import com.bikefinder.auth.domain.valueobject.UserId;
import com.bikefinder.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import com.bikefinder.auth.infrastructure.persistence.entity.UserEntity;
import com.bikefinder.auth.infrastructure.persistence.repository.JpaRefreshTokenRepository;
import com.bikefinder.auth.infrastructure.persistence.repository.JpaUserRepository;
import com.bikefinder.auth.infrastructure.security.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenPortImpl implements RefreshTokenPort {

    private final JpaRefreshTokenRepository refreshTokenRepository;
    private final JpaUserRepository userRepository;

    @Override
    @Transactional
    public String createToken(UserId userId, String familyId) {
        // 1. Generar token plano (aleatorio)
        String rawToken = UUID.randomUUID() + "-" + UUID.randomUUID();

        // 2. Hashear con SHA-256 para almacenamiento (determinístico)
        String tokenHash = TokenHashUtil.hash(rawToken);

        // 3. Cargar usuario
        UserEntity user = userRepository.findById(userId.value())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userId.value()));

        // 4. Crear entidad
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash(tokenHash)  // ← SHA-256 hash
                .familyId(UUID.fromString(familyId))
                .expiresAt(Instant.now().plusSeconds(604800)) // 7 días
                .createdAt(Instant.now())
                .build();

        refreshTokenRepository.save(entity);
        log.debug("Refresh token creado para usuario: {}", userId.value());

        // 5. Retornar token PLANO (solo esta vez)
        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> validateToken(String rawToken) {
        // 1. Hashear el token recibido
        String tokenHash = TokenHashUtil.hash(rawToken);

        // 2. Buscar por hash (indexado, rápido)
        Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            log.warn("Refresh token no encontrado");
            return Optional.empty();
        }

        RefreshTokenEntity token = tokenOpt.get();

        // 3. Verificar expiración y revocación
        if (token.getExpiresAt().isBefore(Instant.now()) || token.getRevokedAt() != null) {
            log.warn("Refresh token inválido: expirado o revocado");
            return Optional.empty();
        }

        return Optional.of(token.getUser().getId().toString());
    }

    @Override
    @Transactional
    public void revokeToken(String rawToken) {
        String tokenHash = TokenHashUtil.hash(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(token);
                    log.info("Refresh token revocado: {}", token.getId());
                });
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(UserId userId) {
        refreshTokenRepository.deleteByUserId(userId.value());
        log.info("Todos los tokens revocados para usuario: {}", userId.value());
    }

    @Override
    @Transactional
    public void revokeFamilyTokens(String familyId) {
        // Implementar según necesites
        log.debug("Revocando familia de tokens: {}", familyId);
    }
}
