package lv.acnbootcamp.fixmycity.controller;

import lv.acnbootcamp.fixmycity.config.SecurityConfig;
import lv.acnbootcamp.fixmycity.dto.audit.AuditLogResponse;
import lv.acnbootcamp.fixmycity.entity.audit.AuditAction;
import lv.acnbootcamp.fixmycity.entity.audit.AuditEntityType;
import lv.acnbootcamp.fixmycity.security.JwtService;
import lv.acnbootcamp.fixmycity.security.UserDetailsServiceImpl;
import lv.acnbootcamp.fixmycity.service.AuditLogService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditLogAdminController.class)
@Import(SecurityConfig.class)
class AuditLogAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private AuditLogResponse sampleResponse() {
        return AuditLogResponse.builder()
                .auditLogId(1L)
                .entityType(AuditEntityType.CATEGORY)
                .entityId(1L)
                .action(AuditAction.CREATE)
                .performedByEmail("admin@example.com")
                .details("Created category 'Roads'")
                .timestamp(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();
    }

    @Nested
    class Authorization {

        @Test
        void getLogs_withoutAuthentication_returns401() throws Exception {
            mockMvc.perform(get("/api/admin/audit-logs"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getLogs_asCitizen_returns403() throws Exception {
            mockMvc.perform(get("/api/admin/audit-logs"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void getLogs_asManager_returns403() throws Exception {
            mockMvc.perform(get("/api/admin/audit-logs"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getLogs_asAdmin_returns200() throws Exception {
            when(auditLogService.getLogs(null, null)).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/admin/audit-logs"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @WithMockUser(roles = "ADMIN")
    class GetLogs {

        @Test
        void returnsAllLogs_whenNoFilterGiven() throws Exception {
            when(auditLogService.getLogs(isNull(), isNull())).thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/admin/audit-logs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].performedByEmail").value("admin@example.com"))
                    .andExpect(jsonPath("$[0].action").value("CREATE"));
        }

        @Test
        void returnsEmptyList_whenNoLogsExist() throws Exception {
            when(auditLogService.getLogs(isNull(), isNull())).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/audit-logs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void filtersByEntityTypeOnly_whenOnlyEntityTypeGiven() throws Exception {
            when(auditLogService.getLogs(eq(AuditEntityType.CATEGORY), isNull()))
                    .thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/admin/audit-logs").param("entityType", "CATEGORY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].entityType").value("CATEGORY"));

            verify(auditLogService).getLogs(AuditEntityType.CATEGORY, null);
        }

        @Test
        void filtersByEntityTypeAndEntityId_whenBothGiven() throws Exception {
            when(auditLogService.getLogs(eq(AuditEntityType.CATEGORY), eq(1L)))
                    .thenReturn(List.of(sampleResponse()));

            mockMvc.perform(get("/api/admin/audit-logs")
                            .param("entityType", "CATEGORY")
                            .param("entityId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].entityId").value(1));

            verify(auditLogService).getLogs(AuditEntityType.CATEGORY, 1L);
        }

        @Test
        void returns400_whenEntityTypeIsInvalidEnumValue() throws Exception {
            mockMvc.perform(get("/api/admin/audit-logs").param("entityType", "NOT_A_TYPE"))
                    .andExpect(status().isBadRequest());

            verify(auditLogService, never()).getLogs(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        }

        @Test
        void returns400_whenEntityIdIsNotNumeric() throws Exception {
            mockMvc.perform(get("/api/admin/audit-logs").param("entityId", "abc"))
                    .andExpect(status().isBadRequest());
        }
    }
}