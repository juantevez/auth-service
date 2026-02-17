package com.bikefinder.auth.infrastructure.security.handler;

import com.bikefinder.auth.application.port.output.JwtTokenPort;
import com.bikefinder.auth.domain.valueobject.UserId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenPort jwtTokenPort;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        // En un caso de uso real, aquí buscarías/crearías el usuario
        // y generarías los tokens. Por ahora, simulamos un UUID.
        // Esto se conectará con el UseCase en la capa de aplicación.
        UUID userId = UUID.randomUUID();

        // Generar tokens (esto se moverá al UseCase)
        String accessToken = jwtTokenPort.generateAccessToken(new UserId(userId), email);
        String refreshToken = jwtTokenPort.generateRefreshToken(new UserId(userId));

        // Redirigir al frontend con los tokens en la URL
        // En producción, mejor usar cookies HttpOnly o enviar al frontend para que los guarde
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/auth/callback")
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshToken)
                .queryParam("token_type", "Bearer")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        log.info("Login OAuth2 exitoso para: {}", email);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
