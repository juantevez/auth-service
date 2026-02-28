package com.bikefinder.auth.application.service;

import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.application.port.input.UpdateUserProfileUseCase;
import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.valueobject.PhoneNumber;
import com.bikefinder.auth.domain.valueobject.UserId;
import com.bikefinder.auth.infrastructure.adapter.in.rest.dto.UpdateProfileRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserProfileServiceImpl implements UpdateUserProfileUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public AuthResponseDto.UserInfoDto execute(UserId userId, UpdateProfileRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));

        // Actualizar campos
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }

        if (request.phoneNumber() != null) {
            user.setPhoneNumber(PhoneNumber.of(request.phoneNumber()));
        }

        if (request.gender() != null) {
            user.setGender(User.Gender.valueOf(request.gender()));
        }

        if (request.birthDate() != null) {
            user.setBirthDate(request.birthDate());
        }

        // Ubicación
        user.setLocalityId(request.localityId());
        user.setLocalityName(request.localityName());
        user.setDepartmentName(request.departmentName());
        user.setProvinceName(request.provinceName());
        user.setCountryName(request.countryName());

        // Guardar
        userRepository.save(user);

        // Retornar perfil actualizado
        return toUserInfoDto(user);
    }

    private AuthResponseDto.UserInfoDto toUserInfoDto(User user) {
        AuthResponseDto.UserInfoDto.LocationDto location = null;
        if (user.getLocalityId() != null || user.getLocalityName() != null) {
            location = new AuthResponseDto.UserInfoDto.LocationDto(
                    user.getLocalityId(),
                    user.getLocalityName(),
                    user.getDepartmentName(),
                    user.getProvinceName(),
                    user.getCountryName()
            );
        }

        return new AuthResponseDto.UserInfoDto(
                user.getId().value().toString(),
                user.getEmail().value(),
                user.getFullName(),
                user.getPhoneNumber() != null ? user.getPhoneNumber().getValue() : null,
                user.isPhoneVerified(),
                user.getAvatarUrl(),
                user.getGender() != null ? user.getGender().name() : null,
                user.getBirthDate(),
                location
        );
    }
}
