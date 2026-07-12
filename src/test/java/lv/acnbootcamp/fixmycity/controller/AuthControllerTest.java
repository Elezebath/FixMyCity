package lv.acnbootcamp.fixmycity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lv.acnbootcamp.fixmycity.config.SecurityConfig;
import lv.acnbootcamp.fixmycity.dto.UserResponse;
import lv.acnbootcamp.fixmycity.entity.Role;
import lv.acnbootcamp.fixmycity.exception.EmailAlreadyExistsException;
import lv.acnbootcamp.fixmycity.security.UserDetailsServiceImpl;
import lv.acnbootcamp.fixmycity.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void register_returns201_whenRequestIsValid() throws Exception {
        var response = new UserResponse(1L, "new@example.com", "New User", Role.CITIZEN);
        when(authService.register(any())).thenReturn(response);

        String requestBody = """
            {
              "email": "new@example.com",
              "password": "password123",
              "fullName": "New User",
              "role": "CITIZEN"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.role").value("CITIZEN"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_returns400_whenEmailIsBlank() throws Exception {
        String requestBody = """
            {
              "email": "",
              "password": "password123",
              "fullName": "New User",
              "role": "CITIZEN"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns409_whenEmailAlreadyExists() throws Exception {
        when(authService.register(any()))
                .thenThrow(new EmailAlreadyExistsException("dup@example.com"));

        String requestBody = """
            {
              "email": "dup@example.com",
              "password": "password123",
              "fullName": "Dup User",
              "role": "CITIZEN"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isConflict());
    }
}