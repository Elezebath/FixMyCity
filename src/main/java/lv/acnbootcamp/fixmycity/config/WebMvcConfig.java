package lv.acnbootcamp.fixmycity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serves uploaded incident attachments at /uploads/** so the frontend can
 * render images/PDFs using the filePath returned in AttachmentResponse.
 *
 * DB stores paths like "/uploads/{uuid}.jpg". Files live under APP_UPLOAD_PATH
 * (or app.upload.path). This maps URL /uploads/** → that directory.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final Path uploadDir;

    public WebMvcConfig(@Value("${app.upload.path:${APP_UPLOAD_PATH:uploads/incidents}}") String uploadPath) {
        this.uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // file:///C:/path/to/uploads/  (trailing slash required)
        String location = uploadDir.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
