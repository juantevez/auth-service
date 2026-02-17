package com.bikefinder.auth.infrastructure.adapter.in.rest;

import com.bikefinder.auth.application.command.*;
import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.application.port.input.*;
import com.bikefinder.auth.infrastructure.adapter.in.rest.dto.LoginRequestDto;
import com.bikefinder.auth.infrastructure.adapter.in.rest.dto.LogoutRequestDto;
import com.bikefinder.auth.infrastructure.adapter.in.rest.dto.RefreshTokenRequestDto;
import com.bikefinder.auth.infrastructure.adapter.in.rest.dto.RegisterRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints de autenticación y gestión de usuarios")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta con email y contraseña")
    public ResponseEntity<AuthResponseDto> register(
            @Valid @RequestBody RegisterRequestDto request) {

        log.info("Solicitud de registro para: {}", request.email());

        RegisterUserCommand command = new RegisterUserCommand(
                request.email(),
                request.password(),
                request.fullName()
        );

        AuthResponseDto response = registerUserUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica usuario con email y contraseña")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest) {

        log.info("Solicitud de login para: {}", request.email());

        LoginUserCommand command = new LoginUserCommand(
                request.email(),
                request.password(),
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        );

        AuthResponseDto response = loginUserUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token", description = "Obtiene nuevo access token usando refresh token")
    public ResponseEntity<AuthResponseDto> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request) {

        log.info("Solicitud de refresh de token");

        RefreshTokenCommand command = new RefreshTokenCommand(request.refreshToken());

        AuthResponseDto response = refreshTokenUseCase.execute(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida el refresh token actual")
    public ResponseEntity<Void> logout(
            @RequestBody LogoutRequestDto request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Solicitud de logout para usuario: {}", userId);

        logoutUseCase.execute(request.refreshToken(), userId);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
