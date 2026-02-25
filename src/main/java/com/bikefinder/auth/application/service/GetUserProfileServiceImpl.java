package com.bikefinder.auth.application.service;

import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.application.port.input.GetUserProfileUseCase;
import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUserProfileServiceImpl implements GetUserProfileUseCase {

    private final UserRepository userRepository;

    @Override
    public AuthResponseDto.UserInfoDto execute(UserId userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + userId));

        return new AuthResponseDto.UserInfoDto(
                user.getId().value().toString(),
                user.getEmail().value(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.isPhoneVerified(),
                user.getAvatarUrl()
        );
    }
}
