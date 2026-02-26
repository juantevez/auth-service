package com.bikefinder.auth.infrastructure.adapter.in.rest;

import com.bikefinder.auth.application.command.*;
import com.bikefinder.auth.application.port.input.*;
import com.bikefinder.auth.infrastructure.adapter.in.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Password & Email", description = "Endpoints de verificación de email y reset de contraseña")
public class PasswordEmailController {

    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ResendVerificationUseCase resendVerificationUseCase;

    @PostMapping("/reset-password/request")
    @Operation(
            summary = "Solicitar reset de contraseña",
            description = "Envía un email con link para resetear la contraseña. Siempre responde 200 aunque el email no exista."
    )
    public ResponseEntity<Void> requestPasswordReset(
            @Valid @RequestBody RequestPasswordResetDto request) {

        log.info("Solicitud de reset de password para: {}", request.email());
        requestPasswordResetUseCase.execute(new RequestPasswordResetCommand(request.email()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password/confirm")
    @Operation(
            summary = "Confirmar reset de contraseña",
            description = "Confirma el reset de contraseña usando el token recibido por email"
    )
    public ResponseEntity<Void> confirmPasswordReset(
            @Valid @RequestBody ConfirmPasswordResetDto request) {

        log.info("Confirmación de reset de password");
        confirmPasswordResetUseCase.execute(
                new ConfirmPasswordResetCommand(request.token(), request.newPassword())
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-email")
    @Operation(
            summary = "Verificar email",
            description = "Verifica el email del usuario usando el token recibido por email"
    )
    public ResponseEntity<Void> verifyEmail(
            @Valid @RequestBody VerifyEmailDto request) {

        log.info("Verificación de email con token");
        verifyEmailUseCase.execute(new VerifyEmailCommand(request.token()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    @Operation(
            summary = "Reenviar email de verificación",
            description = "Reenvía el email de verificación al usuario"
    )
    public ResponseEntity<Void> resendVerification(
            @Valid @RequestBody ResendVerificationDto request) {

        log.info("Reenvío de verificación para: {}", request.email());
        resendVerificationUseCase.execute(new ResendVerificationCommand(request.email()));
        return ResponseEntity.ok().build();
    }
}
