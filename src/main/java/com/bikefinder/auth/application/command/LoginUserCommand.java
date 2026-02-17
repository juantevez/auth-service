package com.bikefinder.auth.application.command;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginUserCommand(
        @NotBlank @Email String email,
        @NotBlank String password,
        String ipAddress,
        String userAgent
) {}
