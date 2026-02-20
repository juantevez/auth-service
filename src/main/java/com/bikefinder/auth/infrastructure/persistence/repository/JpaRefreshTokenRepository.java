package com.bikefinder.auth.infrastructure.persistence.repository;

import com.bikefinder.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    // ✅ Búsqueda por hash (indexada en la BD)
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    // Eliminar todos los tokens de un usuario
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.user.id = :userId")
    int deleteByUserId(@Param("userId") UUID userId);

    // Opcional: Buscar tokens activos de un usuario
    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.user.id = :userId AND r.expiresAt > :now AND r.revokedAt IS NULL")
    List<RefreshTokenEntity> findActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}
