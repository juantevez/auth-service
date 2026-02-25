package com.bikefinder.auth.infrastructure.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class AuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        // Temporal para debug — sacar en producción
        log.error("Fallo en autenticación OAuth2: {}", exception.getMessage());
        log.error("Causa raíz: ", exception); // ← agregar esto, loguea el stack trace completo

        // Agregar esto para ver la cadena completa de causas
        Throwable cause = exception.getCause();
        while (cause != null) {
            log.error("Causado por: {} - {}", cause.getClass().getName(), cause.getMessage());
            cause = cause.getCause();
        }
        String targetUrl = "http://localhost:3000/auth/callback?error=" +
                URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
