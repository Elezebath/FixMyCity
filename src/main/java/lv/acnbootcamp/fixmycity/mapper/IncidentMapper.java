package lv.acnbootcamp.fixmycity.mapper;

import lombok.extern.slf4j.Slf4j;
import lv.acnbootcamp.fixmycity.dto.incident.AttachmentResponse;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.entity.Attachment;
import lv.acnbootcamp.fixmycity.entity.Incident;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IncidentMapper {

    /**
     * Convert Incident Entity to Incident Response DTO
     *
     * @param incident incident details
     * @return IncidentResponse
     */
    public IncidentResponse toResponse(Incident incident) {

        if (incident == null) {
            log.warn("Attempted to map null incident");
            return null;
        }
        return IncidentResponse.builder()
                .incidentId(incident.getIncidentId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .locationAddress(incident.getLocationAddress())
                .categoryId(incident.getCategory().getCategoryId())
                .categoryName(
                        incident.getCategory() != null ? incident.getCategory().getName() : null
                )
                .citizenId(incident.getCitizen().getId())
                .citizenName(
                        incident.getCategory() != null ? incident.getCategory().getName() : null
                )
                .status(
                        incident.getStatus() != null ? incident.getStatus().name() : null
                )
                .priority(
                        incident.getPriority() != null ? incident.getPriority().name() : null
                )
                .createdAt(incident.getCreatedAt())
                .attachment(
                        incident.getAttachments() != null && !incident.getAttachments().isEmpty() ? toAttachmentResponse(
                                incident.getAttachments().get(0)
                        ) : null
                )
                .build();
    }


    /**
     * Convert Attachment Entity to Attachment Response DTO
     *
     * @param attachment attachment details
     * @return AttachmentResponse
     */
    public AttachmentResponse toAttachmentResponse(Attachment attachment) {
        if (attachment == null) {
            return null;
        }
        return AttachmentResponse.builder()
                .attachmentId(attachment.getAttachmentId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .filePath(attachment.getFilePath())
                .build();
    }
}
