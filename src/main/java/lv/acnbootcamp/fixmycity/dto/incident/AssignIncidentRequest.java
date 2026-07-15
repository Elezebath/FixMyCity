package lv.acnbootcamp.fixmycity.dto.incident;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignIncidentRequest {

    @NotNull(message = "Company ID is required.")
    @Positive(message = "Company ID must be positive.")
    private Long companyId;
}