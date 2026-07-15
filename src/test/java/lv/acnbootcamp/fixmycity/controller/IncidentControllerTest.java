package lv.acnbootcamp.fixmycity.controller;

import lv.acnbootcamp.fixmycity.config.SecurityConfig;
import lv.acnbootcamp.fixmycity.dto.incident.AttachmentResponse;
import lv.acnbootcamp.fixmycity.dto.incident.CreateIncidentRequest;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.entity.IncidentPriority;
import lv.acnbootcamp.fixmycity.entity.IncidentStatus;
import lv.acnbootcamp.fixmycity.exception.*;
import lv.acnbootcamp.fixmycity.security.JwtService;
import lv.acnbootcamp.fixmycity.security.UserDetailsServiceImpl;
import lv.acnbootcamp.fixmycity.service.IncidentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
@Import(SecurityConfig.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentService incidentService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    private IncidentResponse createResponse(Long id) {
        return IncidentResponse.builder()
                .incidentId(id)
                .title("Broken Street Light")
                .description("Street light is not working")
                .locationAddress("Riga")
                .categoryName("Infrastructure")
                .categoryId(1L)
                .citizenId(10L)
                .status("NEW")
                .priority("MEDIUM")
                .build();
    }

    private IncidentResponse createResponseWithAttachment(Long id) {
        return IncidentResponse.builder()
                .incidentId(id)
                .title("Broken Street Light")
                .description("Street light is not working")
                .locationAddress("Riga")
                .categoryName("Infrastructure")
                .categoryId(1L)
                .citizenId(10L)
                .status("NEW")
                .priority("MEDIUM")
                .attachment(AttachmentResponse.builder()
                        .attachmentId(1L)
                        .fileName("test.jpg")
                        .filePath("/uploads/test.jpg")
                        .fileType("image/jpeg")
                        .build())
                .build();
    }

    @Nested
    @DisplayName("GET /api/incidents - Find All Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all incidents")
        void shouldReturnAllIncidents() throws Exception {
            when(incidentService.findAll())
                    .thenReturn(List.of(
                            createResponse(1L),
                            createResponse(2L)
                    ));

            mockMvc.perform(get("/api/incidents"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2));

            verify(incidentService).findAll();
        }

        @Test
        @DisplayName("Should return empty incident list")
        void shouldReturnEmptyIncidentList() throws Exception {
            when(incidentService.findAll())
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/incidents"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/incidents/{id} - Find By ID Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return incident by id")
        void shouldReturnIncidentById() throws Exception {
            when(incidentService.findById(1L))
                    .thenReturn(createResponse(1L));

            mockMvc.perform(get("/api/incidents/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.incidentId").value(1))
                    .andExpect(jsonPath("$.title").value("Broken Street Light"));

            verify(incidentService).findById(1L);
        }

        @Test
        @DisplayName("Should return 400 when id is zero")
        void shouldRejectZeroId() throws Exception {
            mockMvc.perform(get("/api/incidents/0"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 400 when id is negative")
        void shouldRejectNegativeId() throws Exception {
            mockMvc.perform(get("/api/incidents/-1"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 404 when incident does not exist")
        void shouldReturn404WhenIncidentMissing() throws Exception {
            when(incidentService.findById(anyLong()))
                    .thenThrow(new IncidentNotFoundException("Incident not found"));

            mockMvc.perform(get("/api/incidents/5"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return incident with attachment")
        void shouldReturnIncidentWithAttachment() throws Exception {
            when(incidentService.findById(1L))
                    .thenReturn(createResponseWithAttachment(1L));

            mockMvc.perform(get("/api/incidents/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.attachment").exists())
                    .andExpect(jsonPath("$.attachment.fileName").value("test.jpg"))
                    .andExpect(jsonPath("$.attachment.fileType").value("image/jpeg"));
        }
    }

    @Nested
    @DisplayName("GET /api/incidents/status/{status} - Find By Status Tests")
    class FindByStatusTests {

        @Test
        @DisplayName("Should find incidents by status NEW")
        void shouldFindByStatusNew() throws Exception {
            when(incidentService.findAllByStatus(IncidentStatus.NEW))
                    .thenReturn(List.of(createResponse(1L)));

            mockMvc.perform(get("/api/incidents/status/NEW"))
                    .andExpect(status().isOk());

            verify(incidentService).findAllByStatus(IncidentStatus.NEW);
        }

        @Test
        @DisplayName("Should find incidents by status IN_PROGRESS")
        void shouldFindByStatusInProgress() throws Exception {
            when(incidentService.findAllByStatus(IncidentStatus.IN_PROGRESS))
                    .thenReturn(List.of(createResponse(1L)));

            mockMvc.perform(get("/api/incidents/status/IN_PROGRESS"))
                    .andExpect(status().isOk());

            verify(incidentService).findAllByStatus(IncidentStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Should reject invalid status")
        void shouldRejectInvalidStatus() throws Exception {
            mockMvc.perform(get("/api/incidents/status/INVALID"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return empty list for status with no incidents")
        void shouldReturnEmptyListForStatusWithNoIncidents() throws Exception {
            when(incidentService.findAllByStatus(IncidentStatus.RESOLVED))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/incidents/status/RESOLVED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/incidents/priority/{priority} - Find By Priority Tests")
    class FindByPriorityTests {

        @Test
        @DisplayName("Should find incidents by priority HIGH")
        void shouldFindByPriorityHigh() throws Exception {
            when(incidentService.findAllByPriority(IncidentPriority.HIGH))
                    .thenReturn(List.of(createResponse(1L)));

            mockMvc.perform(get("/api/incidents/priority/HIGH"))
                    .andExpect(status().isOk());

            verify(incidentService).findAllByPriority(IncidentPriority.HIGH);
        }

        @Test
        @DisplayName("Should find incidents by priority MEDIUM")
        void shouldFindByPriorityMedium() throws Exception {
            when(incidentService.findAllByPriority(IncidentPriority.MEDIUM))
                    .thenReturn(List.of(createResponse(1L), createResponse(2L)));

            mockMvc.perform(get("/api/incidents/priority/MEDIUM"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));

            verify(incidentService).findAllByPriority(IncidentPriority.MEDIUM);
        }

        @Test
        @DisplayName("Should find incidents by priority LOW")
        void shouldFindByPriorityLow() throws Exception {
            when(incidentService.findAllByPriority(IncidentPriority.LOW))
                    .thenReturn(List.of(createResponse(1L)));

            mockMvc.perform(get("/api/incidents/priority/LOW"))
                    .andExpect(status().isOk());

            verify(incidentService).findAllByPriority(IncidentPriority.LOW);
        }

        @Test
        @DisplayName("Should reject invalid priority")
        void shouldRejectInvalidPriority() throws Exception {
            mockMvc.perform(get("/api/incidents/priority/INVALID"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }
    }

    @Nested
    @DisplayName("GET /api/incidents/citizen/{citizenId} - Find By Citizen Tests")
    class FindByCitizenTests {

        @Test
        @DisplayName("Should find incidents by citizen")
        void shouldFindByCitizen() throws Exception {
            when(incidentService.findAllByCitizen(10L))
                    .thenReturn(List.of(createResponse(1L)));

            mockMvc.perform(get("/api/incidents/citizen/10"))
                    .andExpect(status().isOk());

            verify(incidentService).findAllByCitizen(10L);
        }

        @Test
        @DisplayName("Should return multiple incidents for citizen")
        void shouldReturnMultipleIncidentsForCitizen() throws Exception {
            when(incidentService.findAllByCitizen(10L))
                    .thenReturn(List.of(createResponse(1L), createResponse(2L), createResponse(3L)));

            mockMvc.perform(get("/api/incidents/citizen/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3));

            verify(incidentService).findAllByCitizen(10L);
        }

        @Test
        @DisplayName("Should return 400 when citizenId is zero")
        void shouldRejectZeroCitizenId() throws Exception {
            mockMvc.perform(get("/api/incidents/citizen/0"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 400 when citizenId is negative")
        void shouldRejectNegativeCitizenId() throws Exception {
            mockMvc.perform(get("/api/incidents/citizen/-1"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 404 when citizen does not exist")
        void shouldReturn404WhenCitizenNotExists() throws Exception {
            when(incidentService.findAllByCitizen(999L))
                    .thenThrow(new UserNotFoundException("User not found"));

            mockMvc.perform(get("/api/incidents/citizen/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/incidents/company/{companyId} - Find By Company Tests")
    class FindByCompanyTests {

        @Test
        @DisplayName("Should find incidents by company")
        void shouldFindByCompany() throws Exception {
            when(incidentService.findAllByCompany(5L))
                    .thenReturn(List.of(createResponse(1L)));

            mockMvc.perform(get("/api/incidents/company/5"))
                    .andExpect(status().isOk());

            verify(incidentService).findAllByCompany(5L);
        }

        @Test
        @DisplayName("Should return multiple incidents for company")
        void shouldReturnMultipleIncidentsForCompany() throws Exception {
            when(incidentService.findAllByCompany(5L))
                    .thenReturn(List.of(createResponse(1L), createResponse(2L)));

            mockMvc.perform(get("/api/incidents/company/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));

            verify(incidentService).findAllByCompany(5L);
        }

        @Test
        @DisplayName("Should return 400 when companyId is zero")
        void shouldRejectZeroCompanyId() throws Exception {
            mockMvc.perform(get("/api/incidents/company/0"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 400 when companyId is negative")
        void shouldRejectNegativeCompanyId() throws Exception {
            mockMvc.perform(get("/api/incidents/company/-1"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 404 when company does not exist")
        void shouldReturn404WhenCompanyNotExists() throws Exception {
            when(incidentService.findAllByCompany(999L))
                    .thenThrow(new CompanyNotFoundException("Company not found"));

            mockMvc.perform(get("/api/incidents/company/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/incidents/category/{categoryId} - Find By Category Tests")
    class FindByCategoryTests {

        @Test
        @DisplayName("Should find incidents by category")
        void shouldFindByCategory() throws Exception {
            when(incidentService.findAllByCategory(1L))
                    .thenReturn(List.of(createResponse(1L), createResponse(2L)));

            mockMvc.perform(get("/api/incidents/category/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));

            verify(incidentService).findAllByCategory(1L);
        }

        @Test
        @DisplayName("Should return empty list for category with no incidents")
        void shouldReturnEmptyListForCategoryWithNoIncidents() throws Exception {
            when(incidentService.findAllByCategory(1L))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/incidents/category/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(incidentService).findAllByCategory(1L);
        }

        @Test
        @DisplayName("Should return 400 when categoryId is zero")
        void shouldRejectZeroCategoryId() throws Exception {
            mockMvc.perform(get("/api/incidents/category/0"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 400 when categoryId is negative")
        void shouldRejectNegativeCategoryId() throws Exception {
            mockMvc.perform(get("/api/incidents/category/-1"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 404 when category does not exist")
        void shouldReturn404WhenCategoryNotExists() throws Exception {
            when(incidentService.findAllByCategory(999L))
                    .thenThrow(new CategoryNotFoundException("Category not found"));

            mockMvc.perform(get("/api/incidents/category/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/incidents - Create Incident Tests")
    class CreateIncidentTests {

        @Test
        @DisplayName("Should create incident without attachment")
        void shouldCreateIncidentWithoutAttachment() throws Exception {
            when(incidentService.create(any(CreateIncidentRequest.class)))
                    .thenReturn(createResponse(100L));

            mockMvc.perform(multipart("/api/incidents")
                            .param("categoryId", "1")
                            .param("title", "Broken Street Light")
                            .param("description", "The street light is not working")
                            .param("locationAddress", "Brivibas Street 12, Riga")
                            .param("citizenId", "10")
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.incidentId").value(100));

            verify(incidentService).create(any(CreateIncidentRequest.class));
        }

        @Test
        @DisplayName("Should create incident with attachment")
        void shouldCreateIncidentWithAttachment() throws Exception {
            MockMultipartFile attachmentFile = new MockMultipartFile(
                    "attachment",
                    "test.jpg",
                    "image/jpeg",
                    "fake image content".getBytes()
            );

            when(incidentService.create(any(CreateIncidentRequest.class)))
                    .thenReturn(createResponseWithAttachment(100L));

            mockMvc.perform(multipart("/api/incidents")
                            .param("categoryId", "1")
                            .param("title", "Broken Street Light")
                            .param("description", "The street light is not working")
                            .param("locationAddress", "Brivibas Street 12, Riga")
                            .param("citizenId", "10")
                            .file(attachmentFile)
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.incidentId").value(100))
                    .andExpect(jsonPath("$.attachment").exists())
                    .andExpect(jsonPath("$.attachment.fileName").value("test.jpg"));

            verify(incidentService).create(any(CreateIncidentRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when title is missing")
        void shouldReturn400WhenTitleMissing() throws Exception {
            mockMvc.perform(multipart("/api/incidents")
                            .param("categoryId", "1")
                            .param("description", "Description")
                            .param("locationAddress", "Address")
                            .param("citizenId", "10")
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 400 when description is missing")
        void shouldReturn400WhenDescriptionMissing() throws Exception {

            mockMvc.perform(multipart("/api/incidents")
                            .param("categoryId", "1")
                            .param("title", "Title")
                            .param("locationAddress", "Address")
                            .param("citizenId", "10")
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 400 when category is missing")
        void shouldReturn400WhenCategoryMissing() throws Exception {

            mockMvc.perform(multipart("/api/incidents")
                            .param("title", "Title")
                            .param("description", "Description")
                            .param("locationAddress", "Address")
                            .param("citizenId", "10")
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 400 when title is too short")
        void shouldReturn400WhenTitleTooShort() throws Exception {
            mockMvc.perform(multipart("/api/incidents")
                            .file(new MockMultipartFile("categoryId", "", "text/plain", "1".getBytes()))
                            .file(new MockMultipartFile("title", "", "text/plain", "Hi".getBytes()))
                            .file(new MockMultipartFile("description", "", "text/plain", "Description".getBytes()))
                            .file(new MockMultipartFile("locationAddress", "", "text/plain", "Address".getBytes()))
                            .file(new MockMultipartFile("citizenId", "", "text/plain", "10".getBytes()))
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 400 when title is too long")
        void shouldReturn400WhenTitleTooLong() throws Exception {
            String longTitle = "A".repeat(151);
            mockMvc.perform(multipart("/api/incidents")
                            .file(new MockMultipartFile("categoryId", "", "text/plain", "1".getBytes()))
                            .file(new MockMultipartFile("title", "", "text/plain", longTitle.getBytes()))
                            .file(new MockMultipartFile("description", "", "text/plain", "Description".getBytes()))
                            .file(new MockMultipartFile("locationAddress", "", "text/plain", "Address".getBytes()))
                            .file(new MockMultipartFile("citizenId", "", "text/plain", "10".getBytes()))
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 400 when description is too short")
        void shouldReturn400WhenDescriptionTooShort() throws Exception {
            mockMvc.perform(multipart("/api/incidents")
                            .file(new MockMultipartFile("categoryId", "", "text/plain", "1".getBytes()))
                            .file(new MockMultipartFile("title", "", "text/plain", "Title".getBytes()))
                            .file(new MockMultipartFile("description", "", "text/plain", "Short".getBytes()))
                            .file(new MockMultipartFile("locationAddress", "", "text/plain", "Address".getBytes()))
                            .file(new MockMultipartFile("citizenId", "", "text/plain", "10".getBytes()))
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 401 when user is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(multipart("/api/incidents")
                            .file(new MockMultipartFile("categoryId", "", "text/plain", "1".getBytes()))
                            .file(new MockMultipartFile("title", "", "text/plain", "Title".getBytes()))
                            .file(new MockMultipartFile("description", "", "text/plain", "Description".getBytes()))
                            .file(new MockMultipartFile("locationAddress", "", "text/plain", "Address".getBytes()))
                            .file(new MockMultipartFile("citizenId", "", "text/plain", "10".getBytes())))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 403 when user has insufficient role")
        void shouldReturn403WhenInsufficientRole() throws Exception {
            mockMvc.perform(multipart("/api/incidents")
                            .file(new MockMultipartFile("categoryId", "", "text/plain", "1".getBytes()))
                            .file(new MockMultipartFile("title", "", "text/plain", "Title".getBytes()))
                            .file(new MockMultipartFile("description", "", "text/plain", "Description".getBytes()))
                            .file(new MockMultipartFile("locationAddress", "", "text/plain", "Address".getBytes()))
                            .file(new MockMultipartFile("citizenId", "", "text/plain", "10".getBytes()))
                            .with(user("testuser").roles("VIEWER")))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should create incident with CITIZEN role")
        void shouldCreateIncidentWithCitizenRole() throws Exception {
            when(incidentService.create(any(CreateIncidentRequest.class)))
                    .thenReturn(createResponse(100L));

            mockMvc.perform(multipart("/api/incidents")
                            .param("categoryId", "1")
                            .param("title", "Title")
                            .param("description", "Description")
                            .param("locationAddress", "Address")
                            .param("citizenId", "10")
                            .with(user("citizen").roles("CITIZEN")))
                    .andExpect(status().isCreated());

            verify(incidentService).create(any(CreateIncidentRequest.class));
        }

        @Test
        @DisplayName("Should create incident with ADMIN role")
        void shouldCreateIncidentWithAdminRole() throws Exception {
            when(incidentService.create(any(CreateIncidentRequest.class)))
                    .thenReturn(createResponse(100L));

            mockMvc.perform(multipart("/api/incidents")
                            .param("categoryId", "1")
                            .param("title", "Title")
                            .param("description", "Description")
                            .param("locationAddress", "Address")
                            .param("citizenId", "10")
                            .with(user("admin").roles("ADMIN")))
                    .andExpect(status().isCreated());

            verify(incidentService).create(any(CreateIncidentRequest.class));
        }

        @Test
        @DisplayName("Should create incident with MANAGER role")
        void shouldCreateIncidentWithManagerRole() throws Exception {
            when(incidentService.create(any(CreateIncidentRequest.class)))
                    .thenReturn(createResponse(100L));

            mockMvc.perform(multipart("/api/incidents")
                            .param("categoryId", "1")
                            .param("title", "Title")
                            .param("description", "Description")
                            .param("locationAddress", "Address")
                            .param("citizenId", "10")
                            .with(user("manager").roles("MANAGER")))
                    .andExpect(status().isCreated());

            verify(incidentService).create(any(CreateIncidentRequest.class));
        }

        @Test
        @DisplayName("Should return 404 when category not found")
        void shouldReturn404WhenCategoryNotFound() throws Exception {
            when(incidentService.create(any(CreateIncidentRequest.class)))
                    .thenThrow(new CategoryNotFoundException("Category not found"));

            mockMvc.perform(multipart("/api/incidents")
                            .param("categoryId", "999")
                            .param("title", "Title")
                            .param("description", "Description")
                            .param("locationAddress", "Address")
                            .param("citizenId", "10")
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when citizen not found")
        void shouldReturn404WhenCitizenNotFound() throws Exception {
            when(incidentService.create(any(CreateIncidentRequest.class)))
                    .thenThrow(new UserNotFoundException("User not found"));

            mockMvc.perform(multipart("/api/incidents")
                            .param("categoryId", "1")
                            .param("title", "Title")
                            .param("description", "Description")
                            .param("locationAddress", "Address")
                            .param("citizenId", "999")
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when citizenId is zero")
        void shouldReturn400WhenCitizenIdZero() throws Exception {
            mockMvc.perform(multipart("/api/incidents")
                            .file(new MockMultipartFile("categoryId", "", "text/plain", "1".getBytes()))
                            .file(new MockMultipartFile("title", "", "text/plain", "Title".getBytes()))
                            .file(new MockMultipartFile("description", "", "text/plain", "Description".getBytes()))
                            .file(new MockMultipartFile("locationAddress", "", "text/plain", "Address".getBytes()))
                            .file(new MockMultipartFile("citizenId", "", "text/plain", "0".getBytes()))
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }

        @Test
        @DisplayName("Should return 400 when citizenId is negative")
        void shouldReturn400WhenCitizenIdNegative() throws Exception {
            mockMvc.perform(multipart("/api/incidents")
                            .file(new MockMultipartFile("categoryId", "", "text/plain", "1".getBytes()))
                            .file(new MockMultipartFile("title", "", "text/plain", "Title".getBytes()))
                            .file(new MockMultipartFile("description", "", "text/plain", "Description".getBytes()))
                            .file(new MockMultipartFile("locationAddress", "", "text/plain", "Address".getBytes()))
                            .file(new MockMultipartFile("citizenId", "", "text/plain", "-1".getBytes()))
                            .with(user("testuser").roles("CITIZEN")))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(incidentService);
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should return 500 for unexpected errors")
        void shouldReturn500ForUnexpectedError() throws Exception {
            when(incidentService.findAll())
                    .thenThrow(new RuntimeException("Unexpected error"));

            mockMvc.perform(get("/api/incidents"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should return 400 for invalid incident data")
        void shouldReturn400ForInvalidIncidentData() throws Exception {
            when(incidentService.findById(1L))
                    .thenThrow(new InvalidIncidentException("Invalid incident data"));

            mockMvc.perform(get("/api/incidents/1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid priority")
        void shouldReturn400ForInvalidPriority() throws Exception {
            when(incidentService.findAllByPriority(any()))
                    .thenThrow(new InvalidPriorityException("Invalid priority"));

            mockMvc.perform(get("/api/incidents/priority/HIGH"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid status")
        void shouldReturn400ForInvalidStatus() throws Exception {
            when(incidentService.findAllByStatus(any()))
                    .thenThrow(new InvalidStatusException("Invalid status"));

            mockMvc.perform(get("/api/incidents/status/NEW"))
                    .andExpect(status().isBadRequest());
        }
    }
}