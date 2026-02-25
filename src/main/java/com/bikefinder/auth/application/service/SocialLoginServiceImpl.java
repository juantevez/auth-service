package com.bikefinder.auth.application.service;

import com.bikefinder.auth.application.command.SocialLoginCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.application.port.input.SocialLoginUseCase;
import com.bikefinder.auth.application.port.output.AuditLogPort;
import com.bikefinder.auth.application.port.output.JwtTokenPort;
import com.bikefinder.auth.application.port.output.RefreshTokenPort;
import com.bikefinder.auth.application.port.output.UserEventPort;
import com.bikefinder.auth.domain.model.SocialIdentity;
import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.valueobject.Email;
import com.bikefinder.auth.domain.valueobject.UserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class SocialLoginServiceImpl implements SocialLoginUseCase {

    private final UserRepository userRepository;
    private final JwtTokenPort jwtTokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final AuditLogPort auditLogPort;
    private final UserEventPort userEventPort;

    @Value("${auth.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public SocialLoginServiceImpl(UserRepository userRepository,
                                  JwtTokenPort jwtTokenPort,
                                  RefreshTokenPort refreshTokenPort,
                                  AuditLogPort auditLogPort,
                                  UserEventPort userEventPort) {
        this.userRepository = userRepository;
        this.jwtTokenPort = jwtTokenPort;
        this.refreshTokenPort = refreshTokenPort;
        this.auditLogPort = auditLogPort;
        this.userEventPort = userEventPort;
    }

    @Override
    @Transactional
    public AuthResponseDto execute(SocialLoginCommand command) {
        log.info("Login social con proveedor: {}", command.provider());

        Optional<User> existingUser = userRepository.findBySocialIdentity(
                command.provider(),
                command.providerId()
        );

        User user;
        boolean isNewUser = false;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            log.info("Usuario encontrado por identidad social: {}", user.getId());
        } else {
            Optional<User> byEmail = command.email() != null
                    ? userRepository.findByEmail(new Email(command.email()))
                    : Optional.empty();

            if (byEmail.isPresent()) {
                user = byEmail.get();
                user.linkSocialIdentity(new SocialIdentity(command.provider(), command.providerId()));
                userRepository.save(user);
                log.info("Identidad social vinculada a usuario existente: {}", user.getId());
            } else {
                UserId userId = UserId.generate();
                user = User.register(userId, new Email(command.email()), command.fullName());
                user.linkSocialIdentity(new SocialIdentity(command.provider(), command.providerId()));

                if (command.avatarUrl() != null) {
                    user.updateAvatar(command.avatarUrl()); // ← solo updateAvatar, sin save intermedio
                }

                userRepository.save(user); // ← un solo save
                isNewUser = true;
                log.info("Nuevo usuario creado vía social: {}", userId);
            }
        }

        String accessToken  = jwtTokenPort.generateAccessToken(user.getId(), user.getEmail().value());
        //String refreshToken = refreshTokenPort.createToken(user.getId(), command.userAgent()); // ← userAgent en lugar de userId
        String refreshToken = refreshTokenPort.createToken(user.getId(), UUID.randomUUID().toString());

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
                jwtExpirationMs,                           // ← desde config
                Instant.now().plusMillis(jwtExpirationMs), // ← calculado
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
