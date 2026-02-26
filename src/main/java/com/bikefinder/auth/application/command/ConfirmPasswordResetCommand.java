package com.bikefinder.auth.application.command;

public record ConfirmPasswordResetCommand(String token, String newPassword) {}
