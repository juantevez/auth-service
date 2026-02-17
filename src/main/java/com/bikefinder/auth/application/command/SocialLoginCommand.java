package com.bikefinder.auth.application.command;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record SocialLoginCommand(
        @NotBlank String provider,      // GOOGLE, APPLE, FACEBOOK
        @NotBlank String providerId,    // ID del proveedor
        @NotBlank String email,
        String fullName,
        String avatarUrl,
        String accessToken,
        String ipAddress,
        String userAgent,
        Map<String, Object> profileData
) {}
