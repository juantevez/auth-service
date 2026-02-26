package com.bikefinder.auth.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record SocialTokenRequestDto(
        @NotBlank String provider,  // "google", "facebook", "apple"
        @NotBlank String token      // id_token para Google/Apple, access_token para Meta
) {}
