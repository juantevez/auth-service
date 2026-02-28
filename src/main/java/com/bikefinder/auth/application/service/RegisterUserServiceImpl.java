package com.bikefinder.auth.application.service;


import com.bikefinder.auth.application.command.RegisterUserCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.application.port.input.RegisterUserUseCase;
import com.bikefinder.auth.application.port.output.AuditLogPort;
import com.bikefinder.auth.application.port.output.EmailPort;
import com.bikefinder.auth.application.port.output.JwtTokenPort;
import com.bikefinder.auth.application.port.output.PasswordEncoderPort;
import com.bikefinder.auth.application.port.output.RefreshTokenPort;
import com.bikefinder.auth.application.port.output.UserEventPort;
import com.bikefinder.auth.domain.exception.DomainException;
import com.bikefinder.auth.domain.model.Credential;
import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.model.VerificationToken;
import com.bikefinder.auth.domain.model.VerificationTokenType;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.repository.VerificationTokenRepository;
import com.bikefinder.auth.domain.valueobject.Email;
import com.bikefinder.auth.domain.valueobject.UserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class RegisterUserServiceImpl implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtTokenPort jwtTokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final AuditLogPort auditLogPort;
    private final UserEventPort userEventPort;
    private final VerificationTokenRepository tokenRepository;
    private final EmailPort emailPort;

    @Value("${auth.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public RegisterUserServiceImpl(UserRepository userRepository,
                                   PasswordEncoderPort passwordEncoder,
                                   JwtTokenPort jwtTokenPort,
                                   RefreshTokenPort refreshTokenPort,
                                   AuditLogPort auditLogPort,
                                   UserEventPort userEventPort,
                                   VerificationTokenRepository tokenRepository,
                                   EmailPort emailPort) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenPort = jwtTokenPort;
        this.refreshTokenPort = refreshTokenPort;
        this.auditLogPort = auditLogPort;
        this.userEventPort = userEventPort;
        this.tokenRepository = tokenRepository;
        this.emailPort = emailPort;
    }

    @Override
    @Transactional
    public AuthResponseDto execute(RegisterUserCommand command) {
        log.info("Registrando nuevo usuario: {}", command.email());

        // 1. Validar que el email no exista
        Email email = new Email(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new DomainException("El email ya está registrado");
        }

        // 2. Crear usuario
        UserId userId = UserId.generate();
        User user = User.register(userId, email, command.fullName());

        // 3. Crear credenciales
        String passwordHash = passwordEncoder.encode(command.password());
        Credential credential = new Credential(passwordHash);
        user.setCredential(credential);

        // 4. Guardar usuario
        user = userRepository.save(user);

        // 5. Generar y enviar email de verificación
        VerificationToken verificationToken = VerificationToken.create(
                userId,
                VerificationTokenType.EMAIL_VERIFICATION,
                1440 // 24 horas
        );
        tokenRepository.save(verificationToken);
        emailPort.sendVerificationEmail(
                email.value(),
                command.fullName(),
                verificationToken.getToken()
        );

        // 6. Generar tokens JWT
        String accessToken = jwtTokenPort.generateAccessToken(userId, email.value());
        String refreshToken = refreshTokenPort.createToken(userId, UUID.randomUUID().toString());

        // 7. Auditoría y Eventos
        auditLogPort.logAction(userId, "REGISTER", null, Map.of("email", email.value()));
        userEventPort.publishUserRegistered(userId, email.value());

        log.info("Usuario registrado exitosamente: {}", userId);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    private AuthResponseDto buildAuthResponse(User user, String accessToken, String refreshToken) {

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
                accessToken,
                refreshToken,
                "Bearer",
                jwtExpirationMs,
                Instant.now().plusMillis(jwtExpirationMs),
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
