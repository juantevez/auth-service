package com.bikefinder.auth.infrastructure.adapter.in.rest;

import com.bikefinder.auth.application.command.SocialLoginCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.application.port.input.SocialLoginUseCase;
import com.bikefinder.auth.infrastructure.adapter.in.rest.dto.SocialTokenRequestDto;
import com.bikefinder.auth.infrastructure.security.oauth2.SocialTokenValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;


import java.util.Map;

@RestController
@RequestMapping("/auth/oauth2")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OAuth2", description = "Endpoints de autenticación con proveedores sociales")
public class OAuth2Controller {

    private final SocialLoginUseCase socialLoginUseCase;

    private final SocialTokenValidator socialTokenValidator;

    @GetMapping("/success")
    @Operation(summary = "Callback exitoso de OAuth2", description = "Procesa el login exitoso de Google/Facebook/Apple")
    public ResponseEntity<AuthResponseDto> oauth2Success(
            @AuthenticationPrincipal OAuth2User oauth2User,
            Authentication authentication,                    // ← agregar esto
            HttpServletRequest request) {

        log.info("OAuth2 login exitoso");

        // Obtener el provider correctamente desde el token de autenticación
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String provider = authToken.getAuthorizedClientRegistrationId(); // → "google", "facebook", "apple"

        String email    = oauth2User.getAttribute("email");
        String name     = oauth2User.getAttribute("name");
        String picture  = oauth2User.getAttribute("picture");
        String providerId = oauth2User.getName();

        log.info("OAuth2 login exitoso - provider: {}, email: {}", provider, email);

        SocialLoginCommand command = new SocialLoginCommand(
                provider,
                providerId,
                email,
                name,
                picture,
                null,
                getClientIp(request),
                request.getHeader("User-Agent"),
                oauth2User.getAttributes()
        );

        AuthResponseDto response = socialLoginUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/social/token")
    @Operation(summary = "Login mobile/SPA con id_token", description = "Valida el id_token de Google o Apple y retorna JWT propio")
    public ResponseEntity<AuthResponseDto> socialTokenLogin(
            @Valid @RequestBody SocialTokenRequestDto request,
            HttpServletRequest httpRequest) {

        log.info("Social token login - provider: {}", request.provider());

        Map<String, Object> claims = switch (request.provider().toLowerCase()) {
            case "google" -> socialTokenValidator.validateGoogle(request.idToken());
            //case "apple"  -> socialTokenValidator.validateApple(request.idToken());
            default -> throw new IllegalArgumentException("Proveedor no soportado: " + request.provider());
        };

        SocialLoginCommand command = new SocialLoginCommand(
                request.provider().toLowerCase(),
                (String) claims.get("providerId"),
                (String) claims.get("email"),
                (String) claims.get("name"),
                (String) claims.get("picture"),
                null,
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent"),
                claims
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
