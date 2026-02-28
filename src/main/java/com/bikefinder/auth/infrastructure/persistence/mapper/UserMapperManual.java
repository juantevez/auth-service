package com.bikefinder.auth.infrastructure.persistence.mapper;

import com.bikefinder.auth.domain.model.Credential;
import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.valueobject.PhoneNumber;
import com.bikefinder.auth.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapperManual {

    private final UserMapper userMapper;
    private final CredentialMapper credentialMapper;

    /**
     * Mapea UserEntity → User (con credential y nuevos campos de perfil)
     */
    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        // Usar el método factory del dominio
        User user = User.fromPersistence(
                entity.getId(),
                entity.getEmail(),
                entity.getStatus().toString(),
                entity.getFullName(),
                entity.getAvatarUrl()
        );

        // Mapear phoneNumber
        if (entity.getPhoneNumber() != null) {
            user.setPhoneNumber(PhoneNumber.of(entity.getPhoneNumber()));
        }
        user.setPhoneVerified(entity.getPhoneVerified() != null && entity.getPhoneVerified());

        // Mapear nuevos campos de perfil
        if (entity.getGender() != null) {
            user.setGender(User.Gender.valueOf(entity.getGender().toString()));
        }
        user.setBirthDate(entity.getBirthDate());
        user.setLocalityId(entity.getLocalityId());
        user.setLocalityName(entity.getLocalityName());
        user.setDepartmentName(entity.getDepartmentName());
        user.setProvinceName(entity.getProvinceName());
        user.setCountryName(entity.getCountryName());

        // Mapear credential manualmente
        if (entity.getCredential() != null) {
            Credential credential = credentialMapper.toDomain(entity.getCredential());
            user.setCredential(credential);
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
