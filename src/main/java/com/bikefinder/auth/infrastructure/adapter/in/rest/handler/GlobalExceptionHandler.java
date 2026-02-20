package com.bikefinder.auth.infrastructure.adapter.in.rest.handler;

import com.bikefinder.auth.domain.exception.ApplicationException;
import com.bikefinder.auth.domain.exception.DomainException;
import com.bikefinder.auth.domain.exception.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        log.warn("=== INVALID CREDENTIALS ===");
        log.warn("Path: {}", request.getRequestURI());
        log.warn("Message: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto(401, "INVALID_CREDENTIALS", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponseDto> handleLockedAccount(
            LockedException ex, HttpServletRequest request) {
        log.warn("=== ACCOUNT LOCKED ===");
        log.warn("Path: {}", request.getRequestURI());
        log.warn("Message: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.LOCKED)
                .body(new ErrorResponseDto(423, "ACCOUNT_LOCKED", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponseDto> handleDomainException(
            DomainException ex, HttpServletRequest request) {
        log.warn("=== DOMAIN ERROR ===");
        log.warn("Path: {}", request.getRequestURI());
        log.warn("Message: {}", ex.getMessage());
        log.warn("Stack trace:", ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(400, "DOMAIN_ERROR", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponseDto> handleApplicationException(
            ApplicationException ex, HttpServletRequest request) {
        log.error("=== APPLICATION ERROR ===");
        log.error("Path: {}", request.getRequestURI());
        log.error("Message: {}", ex.getMessage());
        log.error("Stack trace:", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(500, "APPLICATION_ERROR", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("=== VALIDATION ERROR ===");
        log.warn("Path: {}", request.getRequestURI());
        log.warn("Errors: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(400, "VALIDATION_ERROR", errors.toString(), Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("=== UNEXPECTED ERROR ===");
        log.error("Path: {}", request.getRequestURI());
        log.error("Method: {}", request.getMethod());
        log.error("Exception type: {}", ex.getClass().getName());
        log.error("Message: {}", ex.getMessage());
        log.error("Stack trace:", ex);

        // Log root cause
        Throwable rootCause = getRootCause(ex);
        if (rootCause != ex) {
            log.error("Root cause type: {}", rootCause.getClass().getName());
            log.error("Root cause message: {}", rootCause.getMessage());
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(
                        500,
                        "INTERNAL_ERROR",
                        ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor",
                        Instant.now(),
                        ex.getClass().getSimpleName(),
                        rootCause.getClass().getSimpleName() + ": " + rootCause.getMessage()
                ));
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    public record ErrorResponseDto(
            int status,
            String code,
            String message,
            Instant timestamp,
            String exception,
            String rootCause
    ) {
        // Constructor para compatibilidad con código existente
        public ErrorResponseDto(int status, String code, String message, Instant timestamp) {
            this(status, code, message, timestamp, null, null);
        }
    }
}
