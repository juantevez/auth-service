package com.bikefinder.auth.infrastructure.persistence.repository;

import com.bikefinder.auth.domain.model.VerificationTokenType;
import com.bikefinder.auth.infrastructure.persistence.entity.VerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface JpaVerificationTokenRepository extends JpaRepository<VerificationTokenEntity, UUID> {

    Optional<VerificationTokenEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM VerificationTokenEntity v WHERE v.expiresAt < :now")
    void deleteExpired(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM VerificationTokenEntity v WHERE v.userId = :userId AND v.type = :type")
    void deleteByUserIdAndType(@Param("userId") UUID userId, @Param("type") VerificationTokenType type);
}
