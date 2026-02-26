package com.bikefinder.auth.domain.repository;

import com.bikefinder.auth.domain.model.VerificationToken;
import com.bikefinder.auth.domain.model.VerificationTokenType;
import com.bikefinder.auth.domain.valueobject.UserId;
import java.util.Optional;

public interface VerificationTokenRepository {
    VerificationToken save(VerificationToken token);
    Optional<VerificationToken> findByToken(String token);
    void deleteExpiredTokens();
    void deleteByUserIdAndType(UserId userId, VerificationTokenType type);
}
