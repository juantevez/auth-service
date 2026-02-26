package com.bikefinder.auth.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestPasswordResetDto(
        @NotBlank @Email String email
) {}
