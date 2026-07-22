package lv.acnbootcamp.fixmycity.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Comment on an incident")
public class CommentResponse {

    @Schema(example = "1")
    private Long commentId;

    @Schema(example = "101")
    private Long incidentId;

    @Schema(example = "Replaced the light fixture and tested it works.")
    private String comment;

    @Schema(example = "Jane Smith")
    private String authorName;

    @Schema(example = "COMPANY")
    private String authorRole;

    @Schema(example = "2026-07-18T15:55:00")
    private LocalDateTime createdAt;
}
