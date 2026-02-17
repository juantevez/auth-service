package com.bikefinder.auth.application.port.output;

import com.bikefinder.auth.domain.valueobject.UserId;

public interface JwtTokenPort {
    String generateAccessToken(UserId userId, String email);
    String generateRefreshToken(UserId userId);
    boolean validateToken(String token);
    UserId extractUserId(String token);
}
