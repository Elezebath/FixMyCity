package lv.acnbootcamp.fixmycity.controller;

import lv.acnbootcamp.fixmycity.config.SecurityConfig;
import lv.acnbootcamp.fixmycity.entity.Company;
import lv.acnbootcamp.fixmycity.repository.CompanyRepository;
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
    private CompanyRepository companyRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    private Company fixItCo() {
        return Company.builder()
                .companyId(1L)
                .companyName("FixIt Co.")
                .registrationNo("LT123456789")
                .contactEmail("contact@fixit.lv")
                .contactPhone("+37060000000")
                .address("Riga, Latvia")
                .active(true)
                .build();
    }

    private Company roadWorks() {
        return Company.builder()
                .companyId(2L)
                .companyName("RoadWorks Ltd.")
                .registrationNo("LT987654321")
                .contactEmail("info@roadworks.lv")
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

            verify(companyRepository, never()).findAllByActiveTrue();
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void findAll_asCitizen_returns403() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isForbidden());

            verify(companyRepository, never()).findAllByActiveTrue();
        }

        @Test
        @WithMockUser(roles = "COMPANY")
        void findAll_asCompany_returns403() throws Exception {
            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isForbidden());

            verify(companyRepository, never()).findAllByActiveTrue();
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void findAll_asManager_returns200() throws Exception {
            when(companyRepository.findAllByActiveTrue()).thenReturn(List.of(fixItCo()));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void findAll_asAdmin_returns200() throws Exception {
            when(companyRepository.findAllByActiveTrue()).thenReturn(List.of(fixItCo()));

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
            when(companyRepository.findAllByActiveTrue()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void findAll_returnsSingleCompany_withMappedFields() throws Exception {
            when(companyRepository.findAllByActiveTrue()).thenReturn(List.of(fixItCo()));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].companyId").value(1))
                    .andExpect(jsonPath("$[0].companyName").value("FixIt Co."))
                    .andExpect(jsonPath("$[0].contactEmail").value("contact@fixit.lv"))
                    .andExpect(jsonPath("$[0].active").value(true));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void findAll_returnsMultipleCompanies_inRepositoryOrder() throws Exception {
            when(companyRepository.findAllByActiveTrue())
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
        void findAll_doesNotExposeInternalFields() throws Exception {
            // Company entity has registrationNo, contactPhone, address, category, user —
            // none of these belong in CompanyResponse. This guards against accidental
            // leakage if the mapping in the controller is ever changed carelessly.
            when(companyRepository.findAllByActiveTrue()).thenReturn(List.of(fixItCo()));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]", aMapWithSize(4)))
                    .andExpect(jsonPath("$[0].registrationNo").doesNotExist())
                    .andExpect(jsonPath("$[0].contactPhone").doesNotExist())
                    .andExpect(jsonPath("$[0].address").doesNotExist())
                    .andExpect(jsonPath("$[0].category").doesNotExist())
                    .andExpect(jsonPath("$[0].user").doesNotExist());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void findAll_handlesCompanyWithNullOptionalFields() throws Exception {
            // contactPhone/address are nullable on the entity; ensure the mapping
            // and JSON serialization don't break when they're absent.
            when(companyRepository.findAllByActiveTrue()).thenReturn(List.of(roadWorks()));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].companyId").value(2))
                    .andExpect(jsonPath("$[0].companyName").value("RoadWorks Ltd."));
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
            when(companyRepository.findAllByActiveTrue()).thenReturn(List.of(fixItCo()));

            mockMvc.perform(get("/api/companies"));

            verify(companyRepository, times(1)).findAllByActiveTrue();
            verifyNoMoreInteractions(companyRepository);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void findAll_propagatesUnexpectedRepositoryException_as500() throws Exception {
            when(companyRepository.findAllByActiveTrue())
                    .thenThrow(new RuntimeException("Unexpected DB failure"));

            mockMvc.perform(get("/api/companies"))
                    .andExpect(status().isInternalServerError());
        }
    }
}