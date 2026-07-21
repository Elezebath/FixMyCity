package lv.acnbootcamp.fixmycity.mapper;

import lombok.extern.slf4j.Slf4j;
import lv.acnbootcamp.fixmycity.dto.incident.AttachmentResponse;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentStatusHistoryResponse;
import lv.acnbootcamp.fixmycity.entity.incident.Attachment;
import lv.acnbootcamp.fixmycity.entity.incident.Comment;
import lv.acnbootcamp.fixmycity.entity.incident.Incident;
import lv.acnbootcamp.fixmycity.entity.incident.IncidentStatusHistory;
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

        Comment latestComment = (incident.getComments() != null && !incident.getComments().isEmpty())
                ? incident.getComments().getFirst()
                : null;

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
                        incident.getCitizen() != null ? incident.getCitizen().getFullName() : null
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
                                incident.getAttachments().getFirst()
                        ) : null
                )
                .assignedCompanyName(
                        incident.getAssignedCompany() != null ? incident.getAssignedCompany().getCompanyName() : null
                )
                .resolvedAt(incident.getResolvedAt())
                .latestComment(latestComment != null ? latestComment.getComment() : null)
                .latestCommentBy(
                        latestComment != null && latestComment.getUser() != null
                                ? latestComment.getUser().getFullName()
                                : null
                )
                .latestCommentAt(latestComment != null ? latestComment.getCreatedAt() : null)
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

    /**
     * Convert IncidentStatusHistory Entity to IncidentStatusHistoryResponse DTO
     *
     * @param history incident status history
     * @param includeChangedBy whether to include changed by or not
     * @return IncidentStatusHistoryResponse
     */
    public IncidentStatusHistoryResponse toStatusHistoryResponse(IncidentStatusHistory history, boolean includeChangedBy) {
        return IncidentStatusHistoryResponse.builder()
                .statusHistoryId(history.getStatusHistoryId())
                .incidentId(history.getIncident().getIncidentId())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .changedByName(includeChangedBy ? history.getChangedBy().getFullName() : null)
                .remarks(history.getRemarks())
                .changedAt(history.getChangedAt())
                .build();
    }
}
