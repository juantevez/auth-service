package com.bikefinder.auth.application.port.input;

public interface LogoutUseCase {
    void execute(String refreshToken, String userId);
}