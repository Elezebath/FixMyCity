package lv.acnbootcamp.fixmycity.controller;

import lv.acnbootcamp.fixmycity.config.SecurityConfig;
import lv.acnbootcamp.fixmycity.service.CompanyService;
import lv.acnbootcamp.fixmycity.dto.company.CompanyResponse;
import lv.acnbootcamp.fixmycity.security.JwtService;
import lv.acnbootcamp.fixmycity.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@Import(SecurityConfig.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    private CompanyResponse fixItCo() {
        return CompanyResponse.builder()
                .companyId(1L)
                .companyName("FixIt Co.")
                .registrationNo("12345")
                .categoryId(1L)
                .contactEmail("contact@fixit.lv")
                .contactPhone("12345678")
                .address("Riga")
                .active(true)
                .build();
    }

    private CompanyResponse roadWorks() {
        return CompanyResponse.builder()
                .companyId(2L)
                .companyName("RoadWorks Ltd.")
                .contactEmail("info@roadworks.lv")
                .registrationNo("67890")
                .categoryId(2L)
                .contactPhone(null)
                .address(null)
                .active(true)
                .build();
    }

    // ---------------------------------------------------------------
    // Authorization: hasAnyRole("MANAGER", "ADMIN")
    // ---------------------------------------------------------------
    @Nested
    class Authorization {

        @Test
        void findAll_withoutAuth_returns401() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isUnauthorized());

            verify(companyService, never()).findAll();
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void findAll_asCitizen_returns403() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isForbidden());

            verify(companyService, never()).findAll();
        }

        @Test
        @WithMockUser(roles = "COMPANY")
        void findAll_asCompany_returns403() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isForbidden());

            verify(companyService, never()).findAll();
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void findAll_asManager_returns200() throws Exception {
            when(companyService.findAll()).thenReturn(List.of(fixItCo()));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void findAll_asAdmin_returns200() throws Exception {
            when(companyService.findAll()).thenReturn(List.of(fixItCo()));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk());
        }
    }

    // ---------------------------------------------------------------
    // Response content and mapping correctness
    // ---------------------------------------------------------------
    @Nested
    class ResponseContent {

        @Test
        @WithMockUser(roles = "MANAGER")
        void findAll_returnsEmptyList_whenNoActiveCompaniesExist() throws Exception {
            when(companyService.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void findAll_returnsSingleCompany_withMappedFields() throws Exception {
            when(companyService.findAll()).thenReturn(List.of(fixItCo()));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].companyId").value(1))
                    .andExpect(jsonPath("$[0].companyName").value("FixIt Co."))
                    .andExpect(jsonPath("$[0].contactEmail").value("contact@fixit.lv"))
                    .andExpect(jsonPath("$[0].active").value(true))
                    .andExpect(jsonPath("$[0].registrationNo").value("12345"))
                    .andExpect(jsonPath("$[0].categoryId").value(1))
                    .andExpect(jsonPath("$[0].contactPhone").value("12345678"))
                    .andExpect(jsonPath("$[0].address").value("Riga"));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void findAll_returnsMultipleCompanies_inRepositoryOrder() throws Exception {
            when(companyService.findAll())
                    .thenReturn(List.of(fixItCo(), roadWorks()));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].companyId").value(1))
                    .andExpect(jsonPath("$[0].companyName").value("FixIt Co."))
                    .andExpect(jsonPath("$[1].companyId").value(2))
                    .andExpect(jsonPath("$[1].companyName").value("RoadWorks Ltd."));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void findAll_exposesOnlyCompanyResponseFields() throws Exception {
            // Ensure only CompanyResponse fields are exposed and internal entity
            // relationships are not leaked.
            when(companyService.findAll()).thenReturn(List.of(fixItCo()));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]", aMapWithSize(8)))
                    .andExpect(jsonPath("$[0].registrationNo").value("12345"))
                    .andExpect(jsonPath("$[0].categoryId").value(1))
                    .andExpect(jsonPath("$[0].contactPhone").value("12345678"))
                    .andExpect(jsonPath("$[0].address").value("Riga"))
                    .andExpect(jsonPath("$[0].category").doesNotExist())
                    .andExpect(jsonPath("$[0].user").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void findAll_handlesCompanyWithNullOptionalFields() throws Exception {
            // contactPhone/address are nullable on the entity; ensure the mapping
            // and JSON serialization don't break when they're absent.
            when(companyService.findAll()).thenReturn(List.of(roadWorks()));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].companyId").value(2))
                    .andExpect(jsonPath("$[0].companyName").value("RoadWorks Ltd."))
                    .andExpect(jsonPath("$[0].contactPhone").isEmpty())
                    .andExpect(jsonPath("$[0].address").isEmpty());
        }
    }

    // ---------------------------------------------------------------
    // Repository interaction
    // ---------------------------------------------------------------
    @Nested
    class RepositoryInteraction {

        @Test
        @WithMockUser(roles = "ADMIN")
        void findAll_callsFindAllByActiveTrue_exactlyOnce() throws Exception {
            when(companyService.findAll()).thenReturn(List.of(fixItCo()));

            mockMvc.perform(get("/api/companies"));

            verify(companyService, times(1)).findAll();
            verifyNoMoreInteractions(companyService);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void findAll_propagatesUnexpectedRepositoryException_as500() throws Exception {
            when(companyService.findAll())
                    .thenThrow(new RuntimeException("Unexpected DB failure"));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isInternalServerError());
        }
    }
}