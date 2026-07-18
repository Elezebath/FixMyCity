package lv.acnbootcamp.fixmycity.controller;

import lv.acnbootcamp.fixmycity.config.SecurityConfig;
import lv.acnbootcamp.fixmycity.dto.category.CategoryResponse;
import lv.acnbootcamp.fixmycity.security.JwtService;
import lv.acnbootcamp.fixmycity.security.UserDetailsServiceImpl;
import lv.acnbootcamp.fixmycity.service.CategoryService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    private CategoryResponse pothole() {
        return CategoryResponse.builder()
                .categoryId(1L)
                .name("Pothole")
                .description("Damaged road surface")
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
    }

    private CategoryResponse streetlight() {
        return CategoryResponse.builder()
                .categoryId(2L)
                .name("Streetlight")
                .description("Broken or flickering streetlight")
                .createdAt(LocalDateTime.of(2026, 1, 2, 9, 30))
                .updatedAt(LocalDateTime.of(2026, 1, 5, 14, 15))
                .build();
    }

    // ---------------------------------------------------------------
    // Authorization: authenticated() — any role, no auth = 401
    // ---------------------------------------------------------------
    @Nested
    class Authorization {

        @Test
        void getAllCategories_withoutAuth_returns401() throws Exception {
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isUnauthorized());

            verify(categoryService, never()).getAllCategories();
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getAllCategories_asCitizen_returns200() throws Exception {
            when(categoryService.getAllCategories()).thenReturn(List.of(pothole()));

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void getAllCategories_asManager_returns200() throws Exception {
            when(categoryService.getAllCategories()).thenReturn(List.of(pothole()));

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "COMPANY")
        void getAllCategories_asCompany_returns200() throws Exception {
            // Rule is authenticated(), not role-restricted — COMPANY must also pass.
            when(categoryService.getAllCategories()).thenReturn(List.of(pothole()));

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getAllCategories_asAdmin_returns200() throws Exception {
            when(categoryService.getAllCategories()).thenReturn(List.of(pothole()));

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk());
        }
    }

    // ---------------------------------------------------------------
    // Response content and structure
    // ---------------------------------------------------------------
    @Nested
    class ResponseContent {

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getAllCategories_returnsEmptyList_whenNoCategoriesExist() throws Exception {
            when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getAllCategories_returnsSingleCategory_withAllFields() throws Exception {
            when(categoryService.getAllCategories()).thenReturn(List.of(pothole()));

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].categoryId").value(1))
                    .andExpect(jsonPath("$[0].name").value("Pothole"))
                    .andExpect(jsonPath("$[0].description").value("Damaged road surface"))
                    .andExpect(jsonPath("$[0].createdAt").exists())
                    .andExpect(jsonPath("$[0].updatedAt").exists());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getAllCategories_returnsMultipleCategories_inServiceOrder() throws Exception {
            when(categoryService.getAllCategories())
                    .thenReturn(List.of(pothole(), streetlight()));

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].categoryId").value(1))
                    .andExpect(jsonPath("$[0].name").value("Pothole"))
                    .andExpect(jsonPath("$[1].categoryId").value(2))
                    .andExpect(jsonPath("$[1].name").value("Streetlight"));
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getAllCategories_doesNotExposeUnexpectedFields() throws Exception {
            // Guards against accidental leakage if CategoryResponse ever gains
            // sensitive fields (e.g. internal flags) without updating this test.
            when(categoryService.getAllCategories()).thenReturn(List.of(pothole()));

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]", aMapWithSize(5)));
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getAllCategories_handlesCategoryWithNullDescription() throws Exception {
            // description is not marked non-null in the entity's DTO;
            // ensure serialization doesn't break on a null value.
            CategoryResponse noDescription = CategoryResponse.builder()
                    .categoryId(3L)
                    .name("Uncategorized")
                    .description(null)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(categoryService.getAllCategories()).thenReturn(List.of(noDescription));

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].description").doesNotExist());
        }
    }

    // ---------------------------------------------------------------
    // Service interaction
    // ---------------------------------------------------------------
    @Nested
    class ServiceInteraction {

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getAllCategories_callsServiceExactlyOnce() throws Exception {
            when(categoryService.getAllCategories()).thenReturn(List.of(pothole()));

            mockMvc.perform(get("/api/categories"));

            verify(categoryService, times(1)).getAllCategories();
            verifyNoMoreInteractions(categoryService);
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getAllCategories_propagatesUnexpectedServiceException_as500() throws Exception {
            when(categoryService.getAllCategories())
                    .thenThrow(new RuntimeException("Unexpected DB failure"));

            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isInternalServerError());
        }
    }
}