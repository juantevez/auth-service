package com.bikefinder.auth.infrastructure.persistence.mapper;

import com.bikefinder.auth.domain.model.Credential;
import com.bikefinder.auth.infrastructure.persistence.entity.CredentialEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CredentialMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    CredentialEntity toEntity(Credential credential);

    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    Credential toDomain(CredentialEntity entity);
}
