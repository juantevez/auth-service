package com.bikefinder.auth.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(min = 2, max = 100) String fullName,
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Formato E.164 requerido")
        String phoneNumber  // ← Nuevo, opcional
) {}
