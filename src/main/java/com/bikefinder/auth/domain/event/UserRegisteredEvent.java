package com.bikefinder.auth.domain.event;

import com.bikefinder.auth.domain.valueobject.UserId;

import java.time.Instant;

// Record para inmutabilidad
public record UserRegisteredEvent(
        UserId userId,
        String email,
        Instant occurredOn
) {
    public UserRegisteredEvent {
        if (occurredOn == null) occurredOn = Instant.now();
    }
}

