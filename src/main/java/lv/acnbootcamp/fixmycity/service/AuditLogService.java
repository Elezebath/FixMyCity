package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.audit.AuditLogResponse;
import lv.acnbootcamp.fixmycity.entity.audit.AuditAction;
import lv.acnbootcamp.fixmycity.entity.audit.AuditEntityType;

import java.util.List;

public interface AuditLogService {
    void log(AuditEntityType entityType, Long entityId, AuditAction action, String details);
    List<AuditLogResponse> getLogs(AuditEntityType entityType, Long entityId);
}