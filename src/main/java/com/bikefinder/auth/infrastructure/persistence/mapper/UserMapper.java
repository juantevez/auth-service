package com.bikefinder.auth.infrastructure.persistence.mapper;

import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.valueobject.*;
import com.bikefinder.auth.infrastructure.persistence.entity.*;
import org.mapstruct.*;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // =============================================================================
    // DOMAIN TO ENTITY (MapStruct genera esto)
    // =============================================================================
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "status", qualifiedByName = "statusToString")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "phoneNumber", source = "phoneNumber.value")
    @Mapping(target = "phoneVerified", source = "phoneVerified")
    @Mapping(target = "gender", qualifiedByName = "genderToString")
    @Mapping(target = "birthDate", source = "birthDate")
    @Mapping(target = "localityId", source = "localityId")
    @Mapping(target = "localityName", source = "localityName")
    @Mapping(target = "departmentName", source = "departmentName")
    @Mapping(target = "provinceName", source = "provinceName")
    @Mapping(target = "countryName", source = "countryName")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "credential", ignore = true)
    @Mapping(target = "socialIdentities", ignore = true)
    UserEntity toEntity(User user);

    // =============================================================================
    // Convertidores de Enum
    // =============================================================================
    @Named("statusToString")
    default String statusToString(UserStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToUserStatus")
    default UserStatus stringToUserStatus(String status) {
        return status != null ? UserStatus.valueOf(status) : UserStatus.ACTIVE;
    }

    @Named("genderToString")
    default String genderToString(User.Gender gender) {
        return gender != null ? gender.name() : null;
    }
}
