package com.bikefinder.auth.infrastructure.persistence.mapper;

import com.bikefinder.auth.domain.model.SocialIdentity;
import com.bikefinder.auth.infrastructure.persistence.entity.SocialIdentityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SocialIdentityMapper {

    // --- DOMAIN TO ENTITY ---
    @Mapping(target = "id", source = "id")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "providerUid", source = "providerUid")
    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tokenExpiresAt", ignore = true)
    @Mapping(target = "profileData", ignore = true)
    SocialIdentityEntity toEntity(SocialIdentity identity);

    // --- ENTITY TO DOMAIN ---
    // Ignoramos propiedades que se gestionan internamente en el dominio
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "providerUid", source = "providerUid")
    SocialIdentity toDomain(SocialIdentityEntity entity);
}
