package lv.acnbootcamp.fixmycity.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Incident response")
public class IncidentResponse {
    @Schema(example = "101")
    private Long incidentId;

    @Schema(example = "Broken street light near Central Park")
    private String title;

    @Schema(example = "The street light has not been working for three days.")
    private String description;

    @Schema(example = "Brivibas Street 12, Riga")
    private String locationAddress;

    @Schema(example = "Infrastructure")
    private String categoryName;

    @Schema(example = "10")
    private Long categoryId;

    @Schema(example = "101")
    private Long citizenId;

    @Schema(example = "John Doe")
    private String citizenName;

    @Schema(example = "NEW")
    private String status;

    @Schema(example = "MEDIUM")
    private String priority;

    @Schema(example = "2026-07-13T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Incident attachment response")
    private AttachmentResponse attachment;

    @Schema(example = "FixIt Co.")
    private String assignedCompanyName;

    @Schema(example = "2026-07-18T16:00:00")
    private LocalDateTime resolvedAt;

    @Schema(example = "Replaced the light fixture and tested it works.")
    private String latestComment;

    @Schema(example = "Jane Smith")
    private String latestCommentBy;

    @Schema(example = "2026-07-18T15:55:00")
    private LocalDateTime latestCommentAt;

}
