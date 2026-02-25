package com.bikefinder.auth.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialTokenRequestDto(
        @NotBlank String provider,  // "google" o "apple"
        @NotBlank String idToken    // token recibido por el SDK mobile/SPA
) {}
