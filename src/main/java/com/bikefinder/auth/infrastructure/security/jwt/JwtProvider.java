package com.bikefinder.auth.infrastructure.security.jwt;

import com.bikefinder.auth.domain.valueobject.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
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
@Slf4j
public class JwtProvider {

    // -------------------------------------------------------------------------
    // Configuración
    // -------------------------------------------------------------------------

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

    /**
     * Identificador de la clave activa.
     * Debe coincidir exactamente con el keyID expuesto en /.well-known/jwks.json.
     * Al rotar claves: cambiar a "auth-service-key-v2", actualizar JWKS y
     * mantener v1 activa en el JWKS durante el período de transición.
     */
    @Value("${auth.jwt.key-id}")
    private String keyId;

    // -------------------------------------------------------------------------
    // Caché de claves — se cargan una sola vez al arrancar el servicio
    // -------------------------------------------------------------------------

    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * Valores del claim "type" para distinguir access tokens de refresh tokens.
     * Evita que un refresh token pueda ser usado como access token y viceversa.
     */
    public static final String TOKEN_TYPE_ACCESS  = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String CLAIM_TOKEN_TYPE  = "type";

    // -------------------------------------------------------------------------
    // Inicialización
    // -------------------------------------------------------------------------

    /**
     * Carga y cachea ambas claves al inicio.
     * De esta forma evitamos I/O + parsing Base64 en cada request.
     */
    @PostConstruct
    public void init() {
        log.info("Inicializando JwtProvider — cargando claves RSA (kid={})", keyId);
        this.privateKey = loadPrivateKey();
        this.publicKey  = loadPublicKey();
        log.info("Claves RSA cargadas correctamente");
    }

    // -------------------------------------------------------------------------
    // Generación de tokens
    // -------------------------------------------------------------------------

    /**
     * Genera un Access Token de vida corta con el email del usuario.
     * Incluye claim "type": "access" para evitar uso incorrecto.
     */
    public String generateAccessToken(UserId userId, String email) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", email);
        extraClaims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        return buildToken(userId.value(), extraClaims, jwtExpirationMs);
    }

    /**
     * Genera un Refresh Token de vida larga.
     * No incluye email ni datos sensibles — solo sirve para renovar access tokens.
     * Incluye claim "type": "refresh" para distinguirlo del access token.
     */
    public String generateRefreshToken(UserId userId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        return buildToken(userId.value(), extraClaims, refreshExpirationMs);
    }

    // -------------------------------------------------------------------------
    // Validación
    // -------------------------------------------------------------------------

    /**
     * Valida firma, expiración y estructura del token.
     * No valida el tipo (access vs refresh) — usar validateAccessToken() para eso.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Valida que el token sea un Access Token válido.
     * Rechaza refresh tokens aunque tengan firma válida.
     */
    public boolean validateAccessToken(String token) {
        if (!validateToken(token)) return false;
        String type = extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
        if (!TOKEN_TYPE_ACCESS.equals(type)) {
            log.warn("Se intentó usar un refresh token como access token");
            return false;
        }
        return true;
    }

    /**
     * Valida que el token sea un Refresh Token válido.
     * Rechaza access tokens aunque tengan firma válida.
     */
    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) return false;
        String type = extractClaim(token, claims -> claims.get(CLAIM_TOKEN_TYPE, String.class));
        if (!TOKEN_TYPE_REFRESH.equals(type)) {
            log.warn("Se intentó usar un access token como refresh token");
            return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Extracción de claims
    // -------------------------------------------------------------------------

    public UUID extractUserId(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        return UUID.fromString(subject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    // -------------------------------------------------------------------------
    // Acceso a la clave pública (usado por JwksController)
    // -------------------------------------------------------------------------

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getKeyId() {
        return keyId;
    }

    // -------------------------------------------------------------------------
    // Implementación interna
    // -------------------------------------------------------------------------

    private String buildToken(UUID userId, Map<String, Object> extraClaims, long expirationMs) {
        Instant now = Instant.now();

        return Jwts.builder()
                .header()
                .keyId(keyId)   // kid en el header → el Gateway lo usa para buscar la clave en JWKS
                .and()
                .claims(extraClaims)
                .subject(userId.toString())
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private PrivateKey loadPrivateKey() {
        try {
            String raw = privateKeyResource.getContentAsString(StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(raw);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar clave privada RSA", e);
        }
    }

    private PublicKey loadPublicKey() {
        try {
            String raw = publicKeyResource.getContentAsString(StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(raw);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(new X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar clave pública RSA", e);
        }
    }
}