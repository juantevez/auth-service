package com.bikefinder.auth.application.service;

import com.bikefinder.auth.application.command.VerifyEmailCommand;
import com.bikefinder.auth.application.port.input.VerifyEmailUseCase;
import com.bikefinder.auth.domain.model.VerificationToken;
import com.bikefinder.auth.domain.model.VerificationTokenType;
import com.bikefinder.auth.domain.model.User;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerifyEmailServiceImpl implements VerifyEmailUseCase {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;

    @Override
    @Transactional
    public void execute(VerifyEmailCommand command) {
        // 1. Buscar y validar token
        VerificationToken token = tokenRepository.findByToken(command.token())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o no existe"));

        if (token.isExpired()) {
            throw new IllegalArgumentException("El token ha expirado");
        }
        if (token.isUsed()) {
            throw new IllegalArgumentException("El token ya fue utilizado");
        }
        if (token.getType() != VerificationTokenType.EMAIL_VERIFICATION) {
            throw new IllegalArgumentException("Token de tipo incorrecto");
        }

        // 2. Marcar email como verificado
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        user.verifyEmail();
        userRepository.save(user);

        // 3. Marcar token como usado
        token.markAsUsed();
        tokenRepository.save(token);

        log.info("Email verificado correctamente para usuario: {}", user.getId());
    }
}