package com.bikefinder.auth.infrastructure.security.jwt;

import com.bikefinder.auth.domain.valueobject.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    @Value("${auth.jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${auth.jwt.public-key-path}")
    private Resource publicKeyResource;

    @Value("${auth.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${auth.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${auth.jwt.issuer}")
    private String issuer;

    @Value("${auth.jwt.audience}")
    private String audience;

    private PrivateKey getPrivateKey() {
        try {
            String key = new String(privateKeyResource.getContentAsString(StandardCharsets.UTF_8));
            key = key.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar clave privada", e);
        }
    }

  public PublicKey getPublicKey() {
      try {
          String key = new String(publicKeyResource.getContentAsString(StandardCharsets.UTF_8));
          key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                  .replace("-----END PUBLIC KEY-----", "")
                  .replaceAll("\\s", "");
          byte[] keyBytes = Base64.getDecoder().decode(key);
          X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
          KeyFactory kf = KeyFactory.getInstance("RSA");
          return kf.generatePublic(spec);
      } catch (Exception e) {
          throw new RuntimeException("Error al cargar clave pública", e);
      }
  }

    public String generateAccessToken(UserId userId, String email) {
        return buildToken(userId.value(), email, jwtExpirationMs);
    }

    public String generateRefreshToken(UserId userId) {
        return buildToken(userId.value(), null, refreshExpirationMs);
    }

    private String buildToken(UUID userId, String email, long expiration) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        if (email != null) {
            claims.put("email", email);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiration)))
                .signWith(getPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getPublicKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.error("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    public UUID extractUserId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return UUID.fromString(subject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
