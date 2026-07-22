package lv.acnbootcamp.fixmycity.config;

import lv.acnbootcamp.fixmycity.controller.HealthController;
import lv.acnbootcamp.fixmycity.security.JwtService;
import lv.acnbootcamp.fixmycity.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HealthController.class)
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    // ---------------------------------------------------------------
    // permitAll endpoints — publicly listed paths
    // ---------------------------------------------------------------
    @Nested
    class PublicEndpoints {

        @Test
        void ping_isAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/ping")).andExpect(status().isOk());
        }

        @Test
        void root_isAccessibleWithoutAuth() throws Exception {
            // No controller maps "/", but security must permit it through
            // (404 from DispatcherServlet, not 401 from security filter)
            mockMvc.perform(get("/"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        void swaggerUi_isAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/swagger-ui.html"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        void apiDocs_isAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/v3/api-docs/some-path"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        void authRegister_isAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(post("/api/auth/register"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        void authLogin_isAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(post("/api/auth/login"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        void authForgotPassword_isAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(post("/api/auth/forgot-password"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        void authResetPassword_isAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(post("/api/auth/reset-password"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        void uploads_isAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/uploads/some-file.jpg"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        void spaAppRoute_isAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/app/dashboard"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }
    }

    // ---------------------------------------------------------------
    // GET /api/incidents/** — public read access
    // ---------------------------------------------------------------
    @Nested
    class IncidentReadAccess {

        @Test
        void getAllIncidents_isAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/incidents"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        void getIncidentById_isAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/incidents/1"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }
    }
    
    // ---------------------------------------------------------------
    // Access control for /api/incidents/my
    // ---------------------------------------------------------------

    @Nested
    class MyIncidentsAccess {

        @Test
        void myIncidents_withoutAuth_returns401() throws Exception {
            mockMvc.perform(get("/api/incidents/my"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void myIncidents_asCitizen_isAccessible() throws Exception {
            mockMvc.perform(get("/api/incidents/my"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void myIncidents_asManager_isAccessible() throws Exception {
            // No role restriction — any authenticated user, not just CITIZEN.
            mockMvc.perform(get("/api/incidents/my"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }
    }

    // ---------------------------------------------------------------
    // GET /api/categories — authenticated, any role
    // ---------------------------------------------------------------
    @Nested
    class CategoryReadAccess {

        @Test
        void getCategories_withoutAuth_returns401() throws Exception {
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getCategories_asCitizen_isNotForbidden() throws Exception {
            // No CategoryController loaded in this slice -> 404, but must NOT be 401/403
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)));
        }
    }

    // ---------------------------------------------------------------
    // PATCH /api/incidents/{id}/assign — MANAGER, ADMIN only
    // ---------------------------------------------------------------
    @Nested
    class AssignIncidentAccess {

        @Test
        void assign_withoutAuth_returns401() throws Exception {
            mockMvc.perform(patch("/api/incidents/1/assign"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void assign_asCitizen_returns403() throws Exception {
            mockMvc.perform(patch("/api/incidents/1/assign"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "COMPANY")
        void assign_asCompany_returns403() throws Exception {
            mockMvc.perform(patch("/api/incidents/1/assign"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void assign_asManager_isNotForbidden() throws Exception {
            mockMvc.perform(patch("/api/incidents/1/assign"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void assign_asAdmin_isNotForbidden() throws Exception {
            mockMvc.perform(patch("/api/incidents/1/assign"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }
    }

    // ---------------------------------------------------------------
    // PATCH /api/incidents/{id}/resolve — COMPANY, MANAGER only
    // NOTE: ADMIN is deliberately NOT included in this rule.
    // ---------------------------------------------------------------
    @Nested
    class ResolveIncidentAccess {

        @Test
        void resolve_withoutAuth_returns401() throws Exception {
            mockMvc.perform(patch("/api/incidents/1/resolve"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void resolve_asCitizen_returns403() throws Exception {
            mockMvc.perform(patch("/api/incidents/1/resolve"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void resolve_asAdmin_returns403() throws Exception {
            // Deliberate: ADMIN is NOT in hasAnyRole("COMPANY", "MANAGER") for resolve.
            // This guards against someone "fixing" this rule by accident later.
            mockMvc.perform(patch("/api/incidents/1/resolve"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "COMPANY")
        void resolve_asCompany_isNotForbidden() throws Exception {
            mockMvc.perform(patch("/api/incidents/1/resolve"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void resolve_asManager_isNotForbidden() throws Exception {
            mockMvc.perform(patch("/api/incidents/1/resolve"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }
    }

    // ---------------------------------------------------------------
    // Other /api/incidents/** methods (e.g. POST create)
    // — CITIZEN, MANAGER, ADMIN only; COMPANY excluded
    // ---------------------------------------------------------------
    @Nested
    class IncidentWriteAccess {

        @Test
        void createIncident_withoutAuth_returns401() throws Exception {
            mockMvc.perform(post("/api/incidents"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "COMPANY")
        void createIncident_asCompany_returns403() throws Exception {
            mockMvc.perform(post("/api/incidents"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void createIncident_asCitizen_isNotForbidden() throws Exception {
            mockMvc.perform(post("/api/incidents"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(403)))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }
    }

    // ---------------------------------------------------------------
    // /api/admin/** — ADMIN only
    // ---------------------------------------------------------------
    @Nested
    class AdminAccess {

        @Test
        void adminUsers_withoutAuth_returns401() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void adminUsers_asCitizen_returns403() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void adminUsers_asManager_returns403() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "COMPANY")
        void adminUsers_asCompany_returns403() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void adminUsers_asAdmin_isAccessible() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isNotFound()); // no controller loaded, but passes auth
        }
    }

    // ---------------------------------------------------------------
    // GET /api/incidents/{id}/comments — authenticated, any role
    // ---------------------------------------------------------------
    @Nested
    class IncidentCommentsReadAccess {

        @Test
        void getComments_withoutAuth_returns401() throws Exception {
            mockMvc.perform(get("/api/incidents/1/comments"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void getIncidentById_stillPublic_whileCommentsRequireAuth() throws Exception {
            mockMvc.perform(get("/api/incidents/1"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
            mockMvc.perform(get("/api/incidents/1/comments"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void getComments_asCitizen_isNotUnauthorized() throws Exception {
            mockMvc.perform(get("/api/incidents/1/comments"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "COMPANY")
        void getComments_asCompany_isNotUnauthorized() throws Exception {
            // No role restriction — any authenticated user, not just CITIZEN.
            mockMvc.perform(get("/api/incidents/1/comments"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void getComments_asManager_isNotUnauthorized() throws Exception {
            mockMvc.perform(get("/api/incidents/1/comments"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getComments_asAdmin_isNotUnauthorized() throws Exception {
            mockMvc.perform(get("/api/incidents/1/comments"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }
    }

    // ---------------------------------------------------------------
    // Fallback: anyRequest().authenticated()
    // ---------------------------------------------------------------
    @Nested
    class FallbackAuthentication {

        @Test
        void unmappedEndpoint_withoutAuth_returns401() throws Exception {
            mockMvc.perform(get("/api/some-unmapped-endpoint"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        void unmappedEndpoint_withAuth_isNotUnauthorized() throws Exception {
            mockMvc.perform(get("/api/some-unmapped-endpoint"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        }
    }

    // ---------------------------------------------------------------
    // Bean-level tests — not MockMvc, just direct bean behavior
    // ---------------------------------------------------------------
    @Nested
    class BeanConfiguration {

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Test
        void passwordEncoder_encodesAndMatchesCorrectly() {
            String raw = "SlaptasZodis123";
            String encoded = passwordEncoder.encode(raw);

            assertThat(encoded).isNotEqualTo(raw);
            assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
            assertThat(passwordEncoder.matches("wrongPassword", encoded)).isFalse();
        }

        @Test
        void passwordEncoder_producesDifferentHashesForSameInput() {
            // BCrypt uses a random salt, so encoding the same password twice
            // must never produce identical hashes.
            String raw = "SlaptasZodis123";
            String encoded1 = passwordEncoder.encode(raw);
            String encoded2 = passwordEncoder.encode(raw);

            assertThat(encoded1).isNotEqualTo(encoded2);
        }
    }

    // ---------------------------------------------------------------
    // CORS configuration — verify allowed origins directly on the bean
    // ---------------------------------------------------------------
    @Nested
    class CorsConfig {

        private final SecurityConfig securityConfig =
                new SecurityConfig(null); // jwtAuthenticationFilter not needed for this bean

        @Test
        void corsConfig_allowsExpectedOrigins() {
            CorsConfiguration config = securityConfig.corsConfigurationSource()
                    .getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest());

            assertThat(config).isNotNull();
            assertThat(config.getAllowedOrigins())
                    .containsExactlyInAnyOrder("http://localhost:3150", "https://team1.acnbootcamp.lv");
            assertThat(config.getAllowCredentials()).isTrue();
        }

        @Test
        void corsConfig_doesNotAllowArbitraryOrigin() {
            CorsConfiguration config = securityConfig.corsConfigurationSource()
                    .getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest());

            assertThat(config).isNotNull();
            assertThat(config.checkOrigin("https://evil-site.com")).isNull();
        }
    }
}