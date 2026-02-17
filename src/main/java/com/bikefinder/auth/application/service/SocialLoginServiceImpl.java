package com.bikefinder.auth.application.service;

import com.bikefinder.auth.application.command.SocialLoginCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.application.port.input.SocialLoginUseCase;
import com.bikefinder.auth.application.port.output.*;
import com.bikefinder.auth.domain.model.SocialIdentity;
import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.valueobject.Email;
import com.bikefinder.auth.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialLoginServiceImpl implements SocialLoginUseCase {

    private final UserRepository userRepository;
    private final JwtTokenPort jwtTokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final AuditLogPort auditLogPort;
    private final UserEventPort userEventPort;

    @Override
    @Transactional
    public AuthResponseDto execute(SocialLoginCommand command) {
        log.info("Login social con proveedor: {}", command.provider());

        // 1. Buscar usuario por identidad social
        Optional<User> existingUser = userRepository.findBySocialIdentity(
                command.provider(),
                command.providerId()
        );

        User user;
        boolean isNewUser = false;

        if (existingUser.isPresent()) {
            // Usuario existente con este proveedor
            user = existingUser.get();
            log.info("Usuario encontrado por identidad social: {}", user.getId());
        } else {
            // 2. Buscar por email (vincular cuentas)
            Optional<User> byEmail = userRepository.findByEmail(new Email(command.email()));

            if (byEmail.isPresent()) {
                // Vincular nueva identidad social a usuario existente
                user = byEmail.get();
                SocialIdentity identity = new SocialIdentity(command.provider(), command.providerId());
                user.linkSocialIdentity(identity);
                userRepository.save(user);
                log.info("Identidad social vinculada a usuario existente: {}", user.getId());
            } else {
                // 3. Crear nuevo usuario
                UserId userId = UserId.generate();
                user = User.register(userId, new Email(command.email()), command.fullName());

                SocialIdentity identity = new SocialIdentity(command.provider(), command.providerId());
                user.linkSocialIdentity(identity);

                if (command.avatarUrl() != null) {
                    // Actualizar avatar desde proveedor
                }

                userRepository.save(user);
                isNewUser = true;
                log.info("Nuevo usuario creado vía social: {}", userId);
            }
        }

        // 4. Generar tokens
        String accessToken = jwtTokenPort.generateAccessToken(user.getId(), user.getEmail().value());
        String refreshToken = refreshTokenPort.createToken(user.getId(), user.getId().value().toString());

        // 5. Auditoría
        auditLogPort.logAction(user.getId(),
                isNewUser ? "REGISTER_SOCIAL" : "LOGIN_SOCIAL",
                command.ipAddress(),
                Map.of("provider", command.provider()));

        if (isNewUser) {
            userEventPort.publishUserRegistered(user.getId(), user.getEmail().value());
        } else {
            userEventPort.publishUserLoggedIn(user.getId());
        }

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
                        user.getAvatarUrl()
                )
        );
    }
}
