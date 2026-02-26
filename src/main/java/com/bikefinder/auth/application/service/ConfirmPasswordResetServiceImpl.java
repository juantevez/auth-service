package com.bikefinder.auth.application.service;

import com.bikefinder.auth.application.command.ConfirmPasswordResetCommand;
import com.bikefinder.auth.application.port.input.ConfirmPasswordResetUseCase;
import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.model.VerificationToken;
import com.bikefinder.auth.domain.model.VerificationTokenType;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmPasswordResetServiceImpl implements ConfirmPasswordResetUseCase {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void execute(ConfirmPasswordResetCommand command) {
        // 1. Buscar y validar token
        VerificationToken token = tokenRepository.findByToken(command.token())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o no existe"));

        if (token.isExpired()) {
            throw new IllegalArgumentException("El token ha expirado");
        }
        if (token.isUsed()) {
            throw new IllegalArgumentException("El token ya fue utilizado");
        }
        if (token.getType() != VerificationTokenType.PASSWORD_RESET) {
            throw new IllegalArgumentException("Token de tipo incorrecto");
        }

        // 2. Buscar usuario
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // 3. Actualizar password
        String encodedPassword = passwordEncoder.encode(command.newPassword());
        user.getCredential().updatePassword(encodedPassword);
        userRepository.save(user);

        // 4. Marcar token como usado
        token.markAsUsed();
        tokenRepository.save(token);

        log.info("Password reseteado correctamente para usuario: {}", user.getId());
    }
}
