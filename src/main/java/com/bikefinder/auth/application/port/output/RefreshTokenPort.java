package com.bikefinder.auth.application.port.output;

import com.bikefinder.auth.domain.valueobject.UserId;

import java.util.Optional;

public interface RefreshTokenPort {
    String createToken(UserId userId, String familyId);
    Optional<String> validateToken(String tokenHash);
    void revokeToken(String tokenHash);
    void revokeAllUserTokens(UserId userId);
    void revokeFamilyTokens(String familyId);
}
