package lv.acnbootcamp.fixmycity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void shouldCreateOpenApiBean() {
        OpenAPI openAPI = config.fixMyCityOpenAPI();

        assertThat(openAPI).isNotNull();
    }

    @Test
    void shouldConfigureApiInfo() {

        OpenAPI openAPI = config.fixMyCityOpenAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("FixMyCity API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("0.0.1");
        assertThat(openAPI.getInfo().getDescription())
                .isEqualTo("Civic incident tracking and infrastructure reporting system");
    }

    @Test
    void shouldConfigureBearerSecurityScheme() {

        OpenAPI openAPI = config.fixMyCityOpenAPI();

        SecurityScheme scheme = openAPI.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");

        assertThat(scheme).isNotNull();
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    void shouldConfigureSecurityRequirement() {

        OpenAPI openAPI = config.fixMyCityOpenAPI();

        assertThat(openAPI.getSecurity()).hasSize(1);

        assertThat(openAPI.getSecurity().getFirst())
                .containsKey("bearerAuth");
    }
}