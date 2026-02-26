package com.bikefinder.auth.infrastructure.persistence.mapper;

import com.bikefinder.auth.domain.model.VerificationToken;
import com.bikefinder.auth.domain.valueobject.UserId;
import com.bikefinder.auth.infrastructure.persistence.entity.VerificationTokenEntity;
import org.springframework.stereotype.Component;

@Component
public class VerificationTokenMapper {

    public VerificationTokenEntity toEntity(VerificationToken domain) {
        return VerificationTokenEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId().value())
                .token(domain.getToken())
                .type(domain.getType())
                .expiresAt(domain.getExpiresAt())
                .usedAt(domain.getUsedAt())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public VerificationToken toDomain(VerificationTokenEntity entity) {
        return VerificationToken.fromPersistence(
                entity.getId(),
                new UserId(entity.getUserId()),
                entity.getToken(),
                entity.getType(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getCreatedAt()
        );
    }
}
