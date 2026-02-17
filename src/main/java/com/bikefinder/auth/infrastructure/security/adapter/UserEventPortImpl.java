package com.bikefinder.auth.infrastructure.security.adapter;

import com.bikefinder.auth.application.port.output.UserEventPort;
import com.bikefinder.auth.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPortImpl implements UserEventPort {

    @Override
    public void publishUserRegistered(UserId userId, String email) {
        // En producción, publicar evento en Kafka/RabbitMQ
        log.info("EVENT: UserRegistered - userId={}, email={}", userId.value(), email);
    }

    @Override
    public void publishUserLoggedIn(UserId userId) {
        log.info("EVENT: UserLoggedIn - userId={}", userId.value());
    }

    @Override
    public void publishPasswordChanged(UserId userId) {
        log.info("EVENT: PasswordChanged - userId={}", userId.value());
    }
}
