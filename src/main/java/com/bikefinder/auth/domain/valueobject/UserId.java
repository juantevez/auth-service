package com.bikefinder.auth.domain.valueobject;

import java.util.UUID;

public record UserId(UUID value) {
    public UserId {
        if (value == null) throw new IllegalArgumentException("El ID de usuario no puede ser nulo");
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
}
