package com.bikefinder.auth.infrastructure.adapter.in.rest;

import com.bikefinder.auth.application.command.SocialLoginCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.application.port.input.SocialLoginUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/oauth2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth2", description = "Endpoints de autenticación con proveedores sociales")
public class OAuth2Controller {

    private final SocialLoginUseCase socialLoginUseCase;

    @GetMapping("/success")
    @Operation(summary = "Callback exitoso de OAuth2", description = "Procesa el login exitoso de Google/Facebook/Apple")
    public ResponseEntity<AuthResponseDto> oauth2Success(
            @AuthenticationPrincipal OAuth2User oauth2User,
            HttpServletRequest request) {

        log.info("OAuth2 login exitoso");

        // Extraer datos del proveedor OAuth2
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");
        String provider = oauth2User.getAttribute("registrationId");
        String providerId = oauth2User.getName();

        SocialLoginCommand command = new SocialLoginCommand(
                provider,
                providerId,
                email,
                name,
                picture,
                null, // access token se maneja internamente
                getClientIp(request),
                request.getHeader("User-Agent"),
                oauth2User.getAttributes()
        );

        AuthResponseDto response = socialLoginUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
