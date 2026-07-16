package lv.acnbootcamp.fixmycity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lv.acnbootcamp.fixmycity.dto.audit.AuditLogResponse;
import lv.acnbootcamp.fixmycity.entity.AuditEntityType;
import lv.acnbootcamp.fixmycity.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@Tag(name = "Audit Logs", description = "Admin-only endpoint for viewing the system action log")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogAdminController {

    private final AuditLogService auditLogService;

    public AuditLogAdminController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Operation(summary = "List audit logs", description = "Optionally filter by entity type and/or entity id. Requires ADMIN role.")
    public ResponseEntity<List<AuditLogResponse>> getLogs(
            @Parameter(description = "Filter by entity type (CATEGORY, USER)") @RequestParam(required = false) AuditEntityType entityType,
            @Parameter(description = "Filter by entity id") @RequestParam(required = false) Long entityId) {
        return ResponseEntity.ok(auditLogService.getLogs(entityType, entityId));
    }
}