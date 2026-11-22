package com.civicai.service;

import com.civicai.entity.AuditLog;
import com.civicai.entity.User;
import com.civicai.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    public void logAction(User user, String action, String entityName, Long entityId, String details, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog(user, action, entityName, entityId, details, ipAddress);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Fail safe, do not crash business transaction
            System.err.println("[Audit] Failed to log action: " + e.getMessage());
        }
    }
}
