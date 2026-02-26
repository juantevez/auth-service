package com.bikefinder.auth.infrastructure.persistence.adapter;

import com.bikefinder.auth.domain.model.VerificationToken;
import com.bikefinder.auth.domain.model.VerificationTokenType;
import com.bikefinder.auth.domain.repository.VerificationTokenRepository;
import com.bikefinder.auth.domain.valueobject.UserId;
import com.bikefinder.auth.infrastructure.persistence.entity.VerificationTokenEntity;
import com.bikefinder.auth.infrastructure.persistence.mapper.VerificationTokenMapper;
import com.bikefinder.auth.infrastructure.persistence.repository.JpaVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class VerificationTokenRepositoryImpl implements VerificationTokenRepository {

    private final JpaVerificationTokenRepository jpaRepository;
    private final VerificationTokenMapper mapper;

    @Override
    @Transactional
    public VerificationToken save(VerificationToken token) {
        VerificationTokenEntity entity = mapper.toEntity(token);
        VerificationTokenEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        jpaRepository.deleteExpired(Instant.now());
        log.info("Tokens expirados eliminados");
    }

    @Override
    @Transactional
    public void deleteByUserIdAndType(UserId userId, VerificationTokenType type) {
        jpaRepository.deleteByUserIdAndType(userId.value(), type);
    }
}
