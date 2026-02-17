package com.bikefinder.auth.infrastructure.security.adapter;

import com.bikefinder.auth.application.port.output.AuditLogPort;
import com.bikefinder.auth.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogPortImpl implements AuditLogPort {

    @Override
    public void logAction(UserId userId, String action, String ipAddress, Map<String, Object> details) {
        // En producción, guardar en DB o enviar a sistema de auditoría
        log.info("AUDIT: userId={}, action={}, ip={}, details={}",
                userId != null ? userId.value() : "ANONYMOUS",
                action,
                ipAddress,
                details);
    }
}
