package com.bikefinder.auth.infrastructure.security.oauth2;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SocialTokenValidator {

    @Value("${oauth2.google.client-id}")
    private String googleClientId;

    private GoogleIdTokenVerifier googleVerifier;

    @PostConstruct
    public void init() {
        googleVerifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

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

    // Apple se agrega en el siguiente paso
}
