package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.audit.AuditLogResponse;
import lv.acnbootcamp.fixmycity.entity.audit.AuditAction;
import lv.acnbootcamp.fixmycity.entity.audit.AuditEntityType;
import lv.acnbootcamp.fixmycity.entity.audit.AuditLog;
import lv.acnbootcamp.fixmycity.repository.AuditLogRepository;
import lv.acnbootcamp.fixmycity.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Nested
    class Log {

        @Test
        void savesEntryWithAuthenticatedUserEmail() {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("admin@example.com", null));

            auditLogService.log(AuditEntityType.CATEGORY, 1L, AuditAction.CREATE, "Created category 'Roads'");

            ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
            verify(auditLogRepository).save(captor.capture());

            AuditLog saved = captor.getValue();
            assertThat(saved.getEntityType()).isEqualTo(AuditEntityType.CATEGORY);
            assertThat(saved.getEntityId()).isEqualTo(1L);
            assertThat(saved.getAction()).isEqualTo(AuditAction.CREATE);
            assertThat(saved.getPerformedByEmail()).isEqualTo("admin@example.com");

            SecurityContextHolder.clearContext();
        }
    }

    @Nested
    class GetLogs {

        @Test
        void returnsAllLogs_whenNoFilterGiven() {
            AuditLog entry = AuditLog.builder()
                    .auditLogId(1L)
                    .entityType(AuditEntityType.CATEGORY)
                    .entityId(1L)
                    .action(AuditAction.DELETE)
                    .performedByEmail("admin@example.com")
                    .timestamp(LocalDateTime.now())
                    .build();

            when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of(entry));

            List<AuditLogResponse> result = auditLogService.getLogs(null, null);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getPerformedByEmail()).isEqualTo("admin@example.com");
        }

        @Test
        void filtersByEntityTypeAndId_whenBothGiven() {
            when(auditLogRepository.findAllByEntityTypeAndEntityIdOrderByTimestampDesc(
                    AuditEntityType.CATEGORY, 1L)).thenReturn(List.of());

            auditLogService.getLogs(AuditEntityType.CATEGORY, 1L);

            verify(auditLogRepository).findAllByEntityTypeAndEntityIdOrderByTimestampDesc(AuditEntityType.CATEGORY, 1L);
            verify(auditLogRepository, never()).findAllByOrderByTimestampDesc();
        }
    }
}