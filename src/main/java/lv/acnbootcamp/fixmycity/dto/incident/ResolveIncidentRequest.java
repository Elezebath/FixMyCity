package lv.acnbootcamp.fixmycity.dto.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolveIncidentRequest {

    @NotBlank(message = "Comment is required when resolving an incident.")
    @Size(min = 5, max = 1000, message = "Comment must be between 5 and 1000 characters.")
    private String comment;
}