package com.bikefinder.auth.application.service;

import com.bikefinder.auth.application.command.RequestPasswordResetCommand;
import com.bikefinder.auth.application.port.input.RequestPasswordResetUseCase;
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
public class RequestPasswordResetServiceImpl implements RequestPasswordResetUseCase {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailPort emailPort;

    @Override
    @Transactional
    public void execute(RequestPasswordResetCommand command) {
        log.info("Solicitud de reset de password para: {}", command.email());

        // Siempre responder OK aunque el email no exista (evita user enumeration)
        userRepository.findByEmail(new Email(command.email())).ifPresent(user -> {
            // Invalidar tokens anteriores del mismo tipo
            tokenRepository.deleteByUserIdAndType(user.getId(), VerificationTokenType.PASSWORD_RESET);

            // Crear nuevo token con 30 minutos de expiración
            VerificationToken token = VerificationToken.create(
                    user.getId(),
                    VerificationTokenType.PASSWORD_RESET,
                    30
            );
            tokenRepository.save(token);

            // Enviar email
            emailPort.sendPasswordResetEmail(
                    user.getEmail().value(),
                    user.getFullName(),
                    token.getToken()
            );

            log.info("Email de reset enviado a: {}", command.email());
        });
    }
}
