package com.bikefinder.auth.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailDto(
        @NotBlank String token
) {}
