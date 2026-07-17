package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.audit.AuditEntityType;
import lv.acnbootcamp.fixmycity.entity.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();
    List<AuditLog> findAllByEntityTypeOrderByTimestampDesc(AuditEntityType entityType);
    List<AuditLog> findAllByEntityIdOrderByTimestampDesc(Long entityId);
    List<AuditLog> findAllByEntityTypeAndEntityIdOrderByTimestampDesc(AuditEntityType entityType, Long entityId);
}