package com.bikefinder.auth.application.service;

import com.bikefinder.auth.application.command.LoginUserCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.application.port.input.LoginUserUseCase;
import com.bikefinder.auth.application.port.output.*;
import com.bikefinder.auth.domain.exception.InvalidCredentialsException;
import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginUserServiceImpl implements LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtTokenPort jwtTokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final AuditLogPort auditLogPort;
    private final UserEventPort userEventPort;

    @Override
    @Transactional
    public AuthResponseDto execute(LoginUserCommand command) {
        log.info("Intento de login para: {}", command.email());

        // 1. Buscar usuario
        User user = userRepository.findByEmail(new Email(command.email()))
                .orElseThrow(() -> {
                    auditLogPort.logAction(null, "LOGIN_FAILED", command.ipAddress(),
                            Map.of("email", command.email(), "reason", "USER_NOT_FOUND"));
                    return new InvalidCredentialsException("Email o contraseña incorrectos");
                });

        // 2. Verificar bloqueo
        if (user.isLocked()) {
            auditLogPort.logAction(user.getId(), "LOGIN_FAILED", command.ipAddress(),
                    Map.of("reason", "ACCOUNT_LOCKED"));
            throw new InvalidCredentialsException("Cuenta temporalmente bloqueada");
        }

        // 3. Validar contraseña
        boolean valid = passwordEncoder.matches(command.password(), user.getCredential().getPasswordHash());

        if (!valid) {
            user.failedLoginAttempt();
            userRepository.save(user);
            auditLogPort.logAction(user.getId(), "LOGIN_FAILED", command.ipAddress(),
                    Map.of("reason", "INVALID_PASSWORD"));
            throw new InvalidCredentialsException("Email o contraseña incorrectos");
        }

        // 4. Login exitoso
        user.successfulLogin();
        userRepository.save(user);

        // 5. Generar tokens
        String accessToken = jwtTokenPort.generateAccessToken(user.getId(), user.getEmail().value());
        String refreshToken = refreshTokenPort.createToken(user.getId(), user.getId().value().toString());

        // 6. Auditoría
        auditLogPort.logAction(user.getId(), "LOGIN_SUCCESS", command.ipAddress(),
                Map.of("method", "PASSWORD"));
        userEventPort.publishUserLoggedIn(user.getId());

        log.info("Login exitoso para: {}", user.getId());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    private AuthResponseDto buildAuthResponse(User user, String accessToken, String refreshToken) {
        return new AuthResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                900000L,
                java.time.Instant.now().plusSeconds(900),
                new AuthResponseDto.UserInfoDto(
                        user.getId().value().toString(),
                        user.getEmail().value(),
                        user.getFullName(),
                        user.getPhoneNumber(),
                        user.isPhoneVerified(),
                        user.getAvatarUrl()
                )
        );
    }
}
