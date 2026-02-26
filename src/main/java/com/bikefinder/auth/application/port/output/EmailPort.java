package com.bikefinder.auth.application.port.output;

public interface EmailPort {
    void sendVerificationEmail(String to, String fullName, String token);
    void sendPasswordResetEmail(String to, String fullName, String token);
}
