package lv.acnbootcamp.fixmycity.dto.audit;

import lombok.*;
import lv.acnbootcamp.fixmycity.entity.audit.AuditAction;
import lv.acnbootcamp.fixmycity.entity.audit.AuditEntityType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long auditLogId;
    private AuditEntityType entityType;
    private Long entityId;
    private AuditAction action;
    private String performedByEmail;
    private String details;
    private LocalDateTime timestamp;
}