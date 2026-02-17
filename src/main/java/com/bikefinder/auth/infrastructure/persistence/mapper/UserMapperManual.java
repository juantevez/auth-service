package com.bikefinder.auth.infrastructure.persistence.mapper;

import com.bikefinder.auth.domain.model.Credential;
import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapperManual {

    private final UserMapper userMapper;
    private final CredentialMapper credentialMapper;

    /**
     * Mapea UserEntity → User (con credential)
     * Método manual porque User no tiene constructor público
     */
    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        System.out.println("=== UserMapperManual.toDomain ===");
        System.out.println("Entity ID: " + entity.getId());
        System.out.println("Entity Credential: " + (entity.getCredential() != null ? "EXISTS" : "NULL"));

        // Usar el método factory del dominio
        User user = User.fromPersistence(
                entity.getId(),
                entity.getEmail(),
                entity.getStatus().toString(),
                entity.getFullName(),
                entity.getAvatarUrl()
        );

        // Mapear credential manualmente
        if (entity.getCredential() != null) {
            Credential credential = credentialMapper.toDomain(entity.getCredential());
            user.setCredential(credential);
            System.out.println("Credential mapeada: " + (user.getCredential() != null ? "OK" : "NULL"));
        }

        return user;
    }

    /**
     * Mapea User → UserEntity (delega a MapStruct)
     */
    public UserEntity toEntity(User user) {
        return userMapper.toEntity(user);
    }
}
