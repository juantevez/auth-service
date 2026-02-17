package com.bikefinder.auth.application.port.output;

import com.bikefinder.auth.domain.valueobject.UserId;
import java.util.Map;

public interface AuditLogPort {
    void logAction(UserId userId, String action, String ipAddress, Map<String, Object> details);
}

