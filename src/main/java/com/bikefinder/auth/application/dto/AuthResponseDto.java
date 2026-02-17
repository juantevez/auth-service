package com.bikefinder.auth.application.dto;

import java.time.Instant;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Instant expiresAt,
        UserInfoDto user
) {
    public record UserInfoDto(
            String id,
            String email,
            String fullName,
            String avatarUrl
    ) {}
}
