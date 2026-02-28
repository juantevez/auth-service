package com.bikefinder.auth.application.service;


import com.bikefinder.auth.application.command.RefreshTokenCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.application.port.input.RefreshTokenUseCase;
import com.bikefinder.auth.application.port.output.JwtTokenPort;
import com.bikefinder.auth.application.port.output.RefreshTokenPort;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenUseCase {

    private final RefreshTokenPort refreshTokenPort;
    private final JwtTokenPort jwtTokenPort;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AuthResponseDto execute(RefreshTokenCommand command) {
        log.info("Refrescando token");

        // 1. Validar refresh token y obtener userId
        String userIdStr = refreshTokenPort.validateToken(command.refreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token inválido o expirado"));

        UserId userId = new UserId(UUID.fromString(userIdStr));

        // 2. Verificar que el usuario existe y está activo
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 3. Revocar token anterior (rotación)
        refreshTokenPort.revokeToken(command.refreshToken());

        // 4. Generar nuevos tokens
        String newAccessToken = jwtTokenPort.generateAccessToken(userId, user.getEmail().value());
        String newRefreshToken = refreshTokenPort.createToken(userId, userId.value().toString());

        log.info("Token refrescado para usuario: {}", userId);

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

        return new AuthResponseDto(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                900000L,
                java.time.Instant.now().plusSeconds(900),
                new AuthResponseDto.UserInfoDto(
                        user.getId().value().toString(),
                        user.getEmail().value(),
                        user.getFullName(),
                        user.getPhoneNumber() != null ? user.getPhoneNumber().getValue() : null,
                        user.isPhoneVerified(),
                        user.getAvatarUrl(),
                        user.getGender() != null ? user.getGender().name() : null,
                        user.getBirthDate(),
                        location
                )
        );
    }
}
