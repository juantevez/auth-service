package com.bikefinder.auth.infrastructure.security.adapter;
import com.bikefinder.auth.application.port.output.JwtTokenPort;
import com.bikefinder.auth.domain.valueobject.UserId;
import com.bikefinder.auth.infrastructure.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenPortImpl implements JwtTokenPort {

    private final JwtProvider jwtProvider;

    @Override
    public String generateAccessToken(UserId userId, String email) {
        return jwtProvider.generateAccessToken(userId, email);
    }

    @Override
    public String generateRefreshToken(UserId userId) {
        return jwtProvider.generateRefreshToken(userId);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtProvider.validateToken(token);
    }

    @Override
    public UserId extractUserId(String token) {
        UUID uuid = jwtProvider.extractUserId(token);
        return new UserId(uuid);
    }
}
