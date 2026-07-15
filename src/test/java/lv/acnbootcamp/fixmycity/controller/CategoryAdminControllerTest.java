package lv.acnbootcamp.fixmycity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lv.acnbootcamp.fixmycity.dto.category.CategoryResponse;
import lv.acnbootcamp.fixmycity.dto.category.CreateCategoryRequest;
import lv.acnbootcamp.fixmycity.dto.category.UpdateCategoryRequest;
import lv.acnbootcamp.fixmycity.exception.category.CategoryAlreadyExistsException;
import lv.acnbootcamp.fixmycity.exception.category.CategoryInUseException;
import lv.acnbootcamp.fixmycity.exception.category.CategoryNotFoundException;
import lv.acnbootcamp.fixmycity.security.JwtService;
import lv.acnbootcamp.fixmycity.security.UserDetailsServiceImpl;
import lv.acnbootcamp.fixmycity.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryAdminController.class)
@Import(lv.acnbootcamp.fixmycity.config.SecurityConfig.class)
class CategoryAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    // Real JwtAuthenticationFilter runs (see USER_MANAGEMENT.md gotcha #2) — only its deps are mocked.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CategoryResponse sampleResponse() {
        return CategoryResponse.builder()
                .categoryId(1L)
                .name("Roads")
                .description("Road issues")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // --- Auth matrix, checked once (shared /api/admin/** rule) ---

    @Test
    void getAllCategories_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CITIZEN")
    void getAllCategories_wrongRole_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isForbidden());
    }

    // --- GET list / by id ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCategories_asAdmin_returns200() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(java.util.List.of(sampleResponse()));

        mockMvc.perform(get("/api/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Roads"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCategoryById_notFound_returns404() throws Exception {
        when(categoryService.getCategoryById(99L))
                .thenThrow(new CategoryNotFoundException("Category not found with id: 99"));

        mockMvc.perform(get("/api/admin/categories/99"))
                .andExpect(status().isNotFound());
    }

    // --- POST ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_validRequest_returns201() throws Exception {
        when(categoryService.createCategory(anyString(), anyString())).thenReturn(sampleResponse());

        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Roads");
        request.setDescription("Road issues");

        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Roads"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_blankName_returns400() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("");
        request.setDescription("Road issues");

        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_duplicateName_returns409() throws Exception {
        when(categoryService.createCategory(anyString(), anyString()))
                .thenThrow(new CategoryAlreadyExistsException("Category already exists with name: Roads"));

        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("Roads");
        request.setDescription("Road issues");

        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // --- PATCH ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_notFound_returns404() throws Exception {
        when(categoryService.updateCategory(anyLong(), anyString(), anyString()))
                .thenThrow(new CategoryNotFoundException("Category not found with id: 99"));

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Roads");
        request.setDescription("Road issues");

        mockMvc.perform(patch("/api/admin/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_duplicateName_returns409() throws Exception {
        when(categoryService.updateCategory(anyLong(), anyString(), anyString()))
                .thenThrow(new CategoryAlreadyExistsException("Category already exists with name: Lighting"));

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("Lighting");
        request.setDescription("desc");

        mockMvc.perform(patch("/api/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // --- DELETE ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_notFound_returns404() throws Exception {
        doThrow(new CategoryNotFoundException("Category not found with id: 99"))
                .when(categoryService).deleteCategory(99L);

        mockMvc.perform(delete("/api/admin/categories/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_inUse_returns409() throws Exception {
        doThrow(new CategoryInUseException("Cannot delete category id 1: it is still referenced by existing incidents"))
                .when(categoryService).deleteCategory(1L);

        mockMvc.perform(delete("/api/admin/categories/1"))
                .andExpect(status().isConflict());
    }
}