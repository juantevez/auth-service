package com.bikefinder.auth.application.service;

import com.bikefinder.auth.application.port.input.LogoutUseCase;
import com.bikefinder.auth.application.port.output.RefreshTokenPort;
import com.bikefinder.auth.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutServiceImpl implements LogoutUseCase {

    private final RefreshTokenPort refreshTokenPort;

    @Override
    @Transactional
    public void execute(String refreshToken, String userId) {
        log.info("Logout para usuario: {}", userId);

        // Revocar token específico
        refreshTokenPort.revokeToken(refreshToken);

        // Opcional: Revocar toda la familia de tokens (logout en todos los dispositivos)
        // refreshTokenPort.revokeAllUserTokens(new UserId(UUID.fromString(userId)));
    }
}
