package com.bikefinder.auth.application.service;


import com.bikefinder.auth.application.command.RegisterUserCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.application.port.input.RegisterUserUseCase;
import com.bikefinder.auth.application.port.output.*;
import com.bikefinder.auth.domain.exception.DomainException;
import com.bikefinder.auth.domain.model.Credential;
import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.valueobject.Email;
import com.bikefinder.auth.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterUserServiceImpl implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtTokenPort jwtTokenPort;
    private final RefreshTokenPort refreshTokenPort;
    private final AuditLogPort auditLogPort;
    private final UserEventPort userEventPort;

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

        // 5. Generar tokens
        String accessToken = jwtTokenPort.generateAccessToken(userId, email.value());
        String refreshToken = refreshTokenPort.createToken(userId, userId.value().toString());

        // 6. Auditoría y Eventos
        auditLogPort.logAction(userId, "REGISTER", null, Map.of("email", email.value()));
        userEventPort.publishUserRegistered(userId, email.value());

        log.info("Usuario registrado exitosamente: {}", userId);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    private AuthResponseDto buildAuthResponse(User user, String accessToken, String refreshToken) {
        return new AuthResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                900000L, // 15 min
                Instant.now().plusSeconds(900),
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
