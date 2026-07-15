package lv.acnbootcamp.fixmycity.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating a new incident")
public class CreateIncidentRequest {

    @NotNull(message = "Category is required.")
    @Positive(message = "Category ID must be positive.")
    @Schema(
            description = "Incident category ID",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long categoryId;

    @NotBlank(message = "Title is required.")
    @Size(min = 5, max = 150, message = "Title must be between 5 and 150 characters.")
    @Schema(
            description = "Incident title",
            example = "Broken street light near Central Park",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String title;

    @NotBlank(message = "Description is required.")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters.")
    @Schema(
            description = "Detailed incident description",
            example = "The street light has not been working for three days.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String description;

    @NotBlank(message = "Location is required.")
    @Size(max = 255)
    @Schema(
            description = "Incident location",
            example = "Brivibas Street 12, Riga",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String locationAddress;

    @Schema(
            description = "Optional attachment file (image or PDF, max 5MB). Can be omitted.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            type = "string",
            format = "binary"
    )
    private MultipartFile attachment;
}
