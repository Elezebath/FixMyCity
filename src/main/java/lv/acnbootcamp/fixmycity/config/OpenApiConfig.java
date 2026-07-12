package lv.acnbootcamp.fixmycity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Provides basic metadata (title, version, description) shown at the top
// of the Swagger UI page. Optional, but makes the generated docs look intentional
// rather than auto-generated defaults.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fixMyCityOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FixMyCity API")
                        .version("0.0.1")
                        .description("Civic incident tracking and infrastructure reporting system"));
    }
}