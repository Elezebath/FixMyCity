package lv.acnbootcamp.fixmycity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lv.acnbootcamp.fixmycity.config.SecurityConfig;
import lv.acnbootcamp.fixmycity.dto.user.UpdateUserRoleRequest;
import lv.acnbootcamp.fixmycity.dto.user.UpdateUserStatusRequest;
import lv.acnbootcamp.fixmycity.dto.user.UserAdminResponse;
import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.exception.user.UserNotFoundException;
import lv.acnbootcamp.fixmycity.security.UserDetailsServiceImpl;
import lv.acnbootcamp.fixmycity.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import lv.acnbootcamp.fixmycity.dto.user.CreateUserByAdminRequest;
import lv.acnbootcamp.fixmycity.dto.user.UpdateUserProfileRequest;
import lv.acnbootcamp.fixmycity.exception.user.EmailAlreadyExistsException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserAdminController.class)
@Import(SecurityConfig.class)
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private lv.acnbootcamp.fixmycity.security.JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Nested
    class Authorization {

        @Test
        void getAllUsers_withoutAuthentication_returns401() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getAllUsers_asCitizen_returns403() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void getAllUsers_asManager_returns403() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getAllUsers_asAdmin_returns200() throws Exception {
            when(userService.getAllUsers()).thenReturn(List.of(buildSampleResponse()));

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @WithMockUser(roles = "ADMIN")
    class GetAllUsers {

        @Test
        void returnsListOfUsers() throws Exception {
            when(userService.getAllUsers()).thenReturn(List.of(buildSampleResponse()));

            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].email").value("citizen@example.com"))
                    .andExpect(jsonPath("$[0].role").value("CITIZEN"));
        }
    }

    @Nested
    @WithMockUser(roles = "ADMIN")
    class GetUserById {

        @Test
        void returnsUserWhenFound() throws Exception {
            when(userService.getUserById(1L)).thenReturn(buildSampleResponse());

            mockMvc.perform(get("/api/admin/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("citizen@example.com"));
        }

        @Test
        void returns404WhenUserNotFound() throws Exception {
            when(userService.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

            mockMvc.perform(get("/api/admin/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("User not found with id: 99"));
        }
    }

    @Nested
    @WithMockUser(roles = "ADMIN")
    class UpdateRole {

        @Test
        void updatesRoleAndReturns200() throws Exception {
            UpdateUserRoleRequest request = new UpdateUserRoleRequest();
            request.setRole(Role.ADMIN);

            UserAdminResponse updated = buildSampleResponse().toBuilder()
                    .role(Role.ADMIN)
                    .build();

            when(userService.updateUserRole(eq(1L), eq(Role.ADMIN))).thenReturn(updated);

            mockMvc.perform(patch("/api/admin/users/1/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        void returns400WhenRoleIsMissing() throws Exception {
            mockMvc.perform(patch("/api/admin/users/1/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns404WhenUserNotFound() throws Exception {
            UpdateUserRoleRequest request = new UpdateUserRoleRequest();
            request.setRole(Role.ADMIN);

            when(userService.updateUserRole(eq(99L), any())).thenThrow(new UserNotFoundException(99L));

            mockMvc.perform(patch("/api/admin/users/99/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @WithMockUser(roles = "ADMIN")
    class UpdateStatus {

        @Test
        void disablesUserAndReturns200() throws Exception {
            UpdateUserStatusRequest request = new UpdateUserStatusRequest();
            request.setEnabled(false);

            UserAdminResponse updated = buildSampleResponse().toBuilder()
                    .enabled(false)
                    .build();

            when(userService.updateUserStatus(eq(1L), eq(false))).thenReturn(updated);

            mockMvc.perform(patch("/api/admin/users/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.enabled").value(false));
        }

        @Test
        void returns400WhenEnabledIsMissing() throws Exception {
            mockMvc.perform(patch("/api/admin/users/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @WithMockUser(roles = "ADMIN")
    class CreateUser {

        @Test
        void createsUserAndReturns201() throws Exception {
            CreateUserByAdminRequest request = new CreateUserByAdminRequest();
            request.setEmail("new@example.com");
            request.setPassword("plainPassword123");
            request.setFullName("New Person");
            request.setRole(Role.MANAGER);

            UserAdminResponse created = buildSampleResponse().toBuilder()
                    .id(5L)
                    .email("new@example.com")
                    .fullName("New Person")
                    .role(Role.MANAGER)
                    .build();

            when(userService.createUser(
                    eq("new@example.com"), eq("plainPassword123"), eq("New Person"), eq(Role.MANAGER), isNull()))
                    .thenReturn(created);

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("new@example.com"))
                    .andExpect(jsonPath("$.role").value("MANAGER"));
        }

        @Test
        void returns400WhenEmailIsBlank() throws Exception {
            CreateUserByAdminRequest request = new CreateUserByAdminRequest();
            request.setEmail("");
            request.setPassword("plainPassword123");
            request.setFullName("New Person");
            request.setRole(Role.CITIZEN);

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns400WhenPasswordTooShort() throws Exception {
            CreateUserByAdminRequest request = new CreateUserByAdminRequest();
            request.setEmail("new@example.com");
            request.setPassword("short");
            request.setFullName("New Person");
            request.setRole(Role.CITIZEN);

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns409WhenEmailAlreadyExists() throws Exception {
            CreateUserByAdminRequest request = new CreateUserByAdminRequest();
            request.setEmail("taken@example.com");
            request.setPassword("plainPassword123");
            request.setFullName("Someone");
            request.setRole(Role.CITIZEN);

            when(userService.createUser(any(), any(), any(), any(), any()))
                    .thenThrow(new EmailAlreadyExistsException("taken@example.com"));

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class CreateUserAuthorization {

        @Test
        void createUser_withoutAuthentication_returns401() throws Exception {
            CreateUserByAdminRequest request = new CreateUserByAdminRequest();
            request.setEmail("new@example.com");
            request.setPassword("plainPassword123");
            request.setFullName("New Person");
            request.setRole(Role.CITIZEN);

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void createUser_asCitizen_returns403() throws Exception {
            CreateUserByAdminRequest request = new CreateUserByAdminRequest();
            request.setEmail("new@example.com");
            request.setPassword("plainPassword123");
            request.setFullName("New Person");
            request.setRole(Role.CITIZEN);

            mockMvc.perform(post("/api/admin/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @WithMockUser(roles = "ADMIN")
    class UpdateProfile {

        @Test
        void updatesProfileAndReturns200() throws Exception {
            UpdateUserProfileRequest request = new UpdateUserProfileRequest();
            request.setEmail("updated@example.com");
            request.setFullName("Updated Name");

            UserAdminResponse updated = buildSampleResponse().toBuilder()
                    .email("updated@example.com")
                    .fullName("Updated Name")
                    .build();

            when(userService.updateUserProfile(eq(1L), eq("updated@example.com"), eq("Updated Name")))
                    .thenReturn(updated);

            mockMvc.perform(patch("/api/admin/users/1/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("updated@example.com"))
                    .andExpect(jsonPath("$.fullName").value("Updated Name"));
        }

        @Test
        void returns400WhenEmailInvalid() throws Exception {
            UpdateUserProfileRequest request = new UpdateUserProfileRequest();
            request.setEmail("not-an-email");
            request.setFullName("Updated Name");

            mockMvc.perform(patch("/api/admin/users/1/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void returns404WhenUserNotFound() throws Exception {
            UpdateUserProfileRequest request = new UpdateUserProfileRequest();
            request.setEmail("x@example.com");
            request.setFullName("X");

            when(userService.updateUserProfile(eq(99L), any(), any()))
                    .thenThrow(new UserNotFoundException(99L));

            mockMvc.perform(patch("/api/admin/users/99/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void returns409WhenEmailAlreadyTaken() throws Exception {
            UpdateUserProfileRequest request = new UpdateUserProfileRequest();
            request.setEmail("taken@example.com");
            request.setFullName("X");

            when(userService.updateUserProfile(eq(1L), eq("taken@example.com"), any()))
                    .thenThrow(new EmailAlreadyExistsException("taken@example.com"));

            mockMvc.perform(patch("/api/admin/users/1/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    private UserAdminResponse buildSampleResponse() {
        return UserAdminResponse.builder()
                .id(1L)
                .email("citizen@example.com")
                .fullName("Jonas Jonaitis")
                .role(Role.CITIZEN)
                .enabled(true)
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();
    }
}