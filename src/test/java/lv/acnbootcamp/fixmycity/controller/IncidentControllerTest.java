package lv.acnbootcamp.fixmycity.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lv.acnbootcamp.fixmycity.config.SecurityConfig;
import lv.acnbootcamp.fixmycity.dto.incident.AssignIncidentRequest;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.dto.incident.ResolveIncidentRequest;
import lv.acnbootcamp.fixmycity.security.JwtAuthenticationFilter;
import lv.acnbootcamp.fixmycity.security.UserDetailsServiceImpl;
import lv.acnbootcamp.fixmycity.service.IncidentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
@Import(SecurityConfig.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private IncidentService incidentService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Nested
    class AssignEndpoint {

        @Test
        void returns401WhenUnauthenticated() throws Exception {
            AssignIncidentRequest request = AssignIncidentRequest.builder().companyId(1L).build();

            mockMvc.perform(patch("/api/incidents/10/assign")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void returns403WhenRoleIsCitizen() throws Exception {
            AssignIncidentRequest request = AssignIncidentRequest.builder().companyId(1L).build();

            mockMvc.perform(patch("/api/incidents/10/assign")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void returns200WhenRoleIsManager() throws Exception {
            AssignIncidentRequest request = AssignIncidentRequest.builder().companyId(1L).build();

            when(incidentService.assignToCompany(anyLong(), any(AssignIncidentRequest.class)))
                    .thenReturn(new IncidentResponse());

            mockMvc.perform(patch("/api/incidents/10/assign")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void returns400WhenCompanyIdMissing() throws Exception {
            AssignIncidentRequest request = AssignIncidentRequest.builder().companyId(null).build();

            mockMvc.perform(patch("/api/incidents/10/assign")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class ResolveEndpoint {

        @Test
        @WithMockUser(roles = "CITIZEN")
        void returns403WhenRoleIsNotAuthorized() throws Exception {
            ResolveIncidentRequest request = ResolveIncidentRequest.builder().comment("Fixed it").build();

            mockMvc.perform(patch("/api/incidents/10/resolve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void returns200WhenRoleIsManager() throws Exception {
            ResolveIncidentRequest request = ResolveIncidentRequest.builder().comment("Fixed it").build();

            when(incidentService.resolveByCompany(anyLong(), any(ResolveIncidentRequest.class), any()))
                    .thenReturn(new IncidentResponse());

            mockMvc.perform(patch("/api/incidents/10/resolve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "COMPANY")
        void returns200WhenRoleIsCompany() throws Exception {
            ResolveIncidentRequest request = ResolveIncidentRequest.builder().comment("Fixed it").build();

            when(incidentService.resolveByCompany(anyLong(), any(ResolveIncidentRequest.class), any()))
                    .thenReturn(new IncidentResponse());

            mockMvc.perform(patch("/api/incidents/10/resolve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "COMPANY")
        void returns400WhenCommentIsBlank() throws Exception {
            ResolveIncidentRequest request = ResolveIncidentRequest.builder().comment("").build();

            mockMvc.perform(patch("/api/incidents/10/resolve")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}