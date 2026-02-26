package com.bikefinder.auth.application.service;

import com.bikefinder.auth.application.command.ResendVerificationCommand;
import com.bikefinder.auth.application.port.input.ResendVerificationUseCase;
import com.bikefinder.auth.application.port.output.EmailPort;
import com.bikefinder.auth.domain.model.VerificationToken;
import com.bikefinder.auth.domain.model.VerificationTokenType;
import com.bikefinder.auth.domain.repository.UserRepository;
import com.bikefinder.auth.domain.repository.VerificationTokenRepository;
import com.bikefinder.auth.domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResendVerificationServiceImpl implements ResendVerificationUseCase {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailPort emailPort;

    @Override
    @Transactional
    public void execute(ResendVerificationCommand command) {
        log.info("Reenvío de verificación para: {}", command.email());

        userRepository.findByEmail(new Email(command.email())).ifPresent(user -> {
            if (user.isEmailVerified()) {
                throw new IllegalStateException("El email ya está verificado");
            }

            // Invalidar tokens anteriores
            tokenRepository.deleteByUserIdAndType(user.getId(), VerificationTokenType.EMAIL_VERIFICATION);

            // Crear nuevo token con 24 horas de expiración
            VerificationToken token = VerificationToken.create(
                    user.getId(),
                    VerificationTokenType.EMAIL_VERIFICATION,
                    1440 // 24 horas
            );
            tokenRepository.save(token);

            emailPort.sendVerificationEmail(
                    user.getEmail().value(),
                    user.getFullName(),
                    token.getToken()
            );

            log.info("Email de verificación reenviado a: {}", command.email());
        });
    }
}
