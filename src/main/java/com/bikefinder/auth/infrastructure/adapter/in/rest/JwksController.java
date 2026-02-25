package com.bikefinder.auth.infrastructure.adapter.in.rest;

import com.bikefinder.auth.infrastructure.security.jwt.JwtProvider;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Hidden // No mostrar en Swagger (es endpoint técnico)
public class JwksController {

    private final JwtProvider jwtProvider;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> getJwks() throws Exception {
        // Obtener la clave pública del JwtProvider
        RSAPublicKey publicKey = (RSAPublicKey) jwtProvider.getPublicKey();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyID(jwtProvider.getKeyId())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return jwkSet.toJSONObject();
    }
}
