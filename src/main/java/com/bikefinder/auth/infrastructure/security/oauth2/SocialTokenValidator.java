package com.bikefinder.auth.infrastructure.security.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SocialTokenValidator {

    // -------------------------------------------------------------------------
    // Google
    // -------------------------------------------------------------------------

    @Value("${oauth2.google.client-id}")
    private String googleClientId;

    private GoogleIdTokenVerifier googleVerifier;

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    @Value("${oauth2.facebook.app-id}")
    private String facebookAppId;

    @Value("${oauth2.facebook.app-secret}")
    private String facebookAppSecret;

    private RestClient restClient;

    private final ObjectMapper objectMapper = new ObjectMapper();


    // -------------------------------------------------------------------------
    // Inicialización
    // -------------------------------------------------------------------------

    @PostConstruct
    public void init() {
        // Google verifier
        googleVerifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        // RestClient para Meta Graph API
        restClient = RestClient.builder()
                .baseUrl("https://graph.facebook.com")
                .build();
    }

    // -------------------------------------------------------------------------
    // Google
    // -------------------------------------------------------------------------

    public Map<String, Object> validateGoogle(String idToken) {
        try {
            GoogleIdToken token = googleVerifier.verify(idToken);
            if (token == null) {
                throw new IllegalArgumentException("Google id_token inválido o expirado");
            }

            GoogleIdToken.Payload payload = token.getPayload();
            Map<String, Object> claims = new HashMap<>();
            claims.put("providerId", payload.getSubject());
            claims.put("email",      payload.getEmail());
            claims.put("name",       payload.get("name"));
            claims.put("picture",    payload.get("picture"));
            return claims;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validando Google id_token: {}", e.getMessage());
            throw new IllegalArgumentException("Error al validar token de Google", e);
        }
    }

    // -------------------------------------------------------------------------
    // Meta
    // -------------------------------------------------------------------------

    /**
     * Valida un access_token de Meta llamando a la Graph API.
     * A diferencia de Google, Meta no usa JWT — el token es opaco
     * y se valida haciendo una llamada HTTP al endpoint /me.
     *
     * Primero verifica que el token fue emitido para tu app (debug_token),
     * luego obtiene los datos del usuario (/me).
     */
    public Map<String, Object> validateMeta(String accessToken) {
        try {
            // Paso 1 — verificar que el token pertenece a tu app
            verifyMetaToken(accessToken);

            // Paso 2 — obtener datos del usuario
            return fetchMetaUserData(accessToken);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error validando Meta access_token: {}", e.getMessage());
            throw new IllegalArgumentException("Error al validar token de Meta", e);
        }
    }

    private void verifyMetaToken(String accessToken) {
        String appToken = facebookAppId + "|" + facebookAppSecret;

        String raw = restClient.get()
                .uri("/debug_token?input_token={token}&access_token={appToken}",
                        accessToken, appToken)
                .retrieve()
                .body(String.class); // ← String en lugar de Map

        try {
            Map<String, Object> debugResponse = objectMapper.readValue(raw, Map.class);
            Map<String, Object> data = (Map<String, Object>) debugResponse.get("data");

            if (data == null) {
                throw new IllegalArgumentException("Respuesta inválida de Meta debug_token");
            }

            Boolean isValid = (Boolean) data.get("is_valid");
            if (!Boolean.TRUE.equals(isValid)) {
                throw new IllegalArgumentException("Meta access_token inválido o expirado");
            }

            String tokenAppId = String.valueOf(data.get("app_id"));
            if (!facebookAppId.equals(tokenAppId)) {
                throw new IllegalArgumentException("Meta access_token no pertenece a esta aplicación");
            }

            log.debug("Meta token verificado correctamente para app: {}", tokenAppId);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parseando respuesta de Meta debug_token", e);
        }
    }

    private Map<String, Object> fetchMetaUserData(String accessToken) {
        String raw = restClient.get()
                .uri("/me?fields=id,name,email,picture.type(large)&access_token={token}",
                        accessToken)
                .retrieve()
                .body(String.class); // ← String en lugar de Map

        try {
            Map<String, Object> userData = objectMapper.readValue(raw, Map.class);

            if (userData == null || userData.get("id") == null) {
                throw new IllegalArgumentException("No se pudieron obtener datos del usuario de Meta");
            }

            String pictureUrl = null;
            Map<String, Object> picture = (Map<String, Object>) userData.get("picture");
            if (picture != null) {
                Map<String, Object> pictureData = (Map<String, Object>) picture.get("data");
                if (pictureData != null) {
                    pictureUrl = (String) pictureData.get("url");
                }
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("providerId", String.valueOf(userData.get("id")));
            claims.put("email",      userData.get("email"));
            claims.put("name",       userData.get("name"));
            claims.put("picture",    pictureUrl);

            log.debug("Meta user data obtenida - id: {}, email: {}",
                    claims.get("providerId"), claims.get("email"));

            return claims;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parseando respuesta de Meta /me", e);
        }
    }

}
