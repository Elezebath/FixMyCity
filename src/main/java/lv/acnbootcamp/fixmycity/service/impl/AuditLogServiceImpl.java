package lv.acnbootcamp.fixmycity.service.impl;

import lombok.extern.slf4j.Slf4j;
import lv.acnbootcamp.fixmycity.dto.audit.AuditLogResponse;
import lv.acnbootcamp.fixmycity.entity.AuditAction;
import lv.acnbootcamp.fixmycity.entity.AuditEntityType;
import lv.acnbootcamp.fixmycity.entity.AuditLog;
import lv.acnbootcamp.fixmycity.repository.AuditLogRepository;
import lv.acnbootcamp.fixmycity.service.AuditLogService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void log(AuditEntityType entityType, Long entityId, AuditAction action, String details) {
        String performedBy = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEM";

        AuditLog entry = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedByEmail(performedBy)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(entry);
        log.info("Audit: {} {} id={} by={}", action, entityType, entityId, performedBy);
    }

    @Override
    public List<AuditLogResponse> getLogs(AuditEntityType entityType, Long entityId) {
        List<AuditLog> results;
        if (entityType != null && entityId != null) {
            results = auditLogRepository.findAllByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
        } else if (entityType != null) {
            results = auditLogRepository.findAllByEntityTypeOrderByTimestampDesc(entityType);
        } else if (entityId != null) {
            results = auditLogRepository.findAllByEntityIdOrderByTimestampDesc(entityId);
        } else {
            results = auditLogRepository.findAllByOrderByTimestampDesc();
        }
        return results.stream().map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLog entry) {
        return AuditLogResponse.builder()
                .auditLogId(entry.getAuditLogId())
                .entityType(entry.getEntityType())
                .entityId(entry.getEntityId())
                .action(entry.getAction())
                .performedByEmail(entry.getPerformedByEmail())
                .details(entry.getDetails())
                .timestamp(entry.getTimestamp())
                .build();
    }
}