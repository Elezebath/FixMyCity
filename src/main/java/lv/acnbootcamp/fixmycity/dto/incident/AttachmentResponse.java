package lv.acnbootcamp.fixmycity.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Attachment information")
public class AttachmentResponse {
    @Schema(example = "15")
    private Long attachmentId;

    @Schema(example = "street-light.jpg")
    private String fileName;

    @Schema(example = "image/jpeg")
    private String fileType;

    @Schema(example = "/uploads/street-light.jpg")
    private String filePath;
}
