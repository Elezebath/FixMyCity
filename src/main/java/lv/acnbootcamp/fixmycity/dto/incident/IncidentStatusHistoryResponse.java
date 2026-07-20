package lv.acnbootcamp.fixmycity.dto.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lv.acnbootcamp.fixmycity.entity.incident.IncidentStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentStatusHistoryResponse {

    private Long statusHistoryId;
    private Long incidentId;
    private IncidentStatus oldStatus;
    private IncidentStatus newStatus;

    /**
     * Only when the requester is COMPANY, MANAGER, or ADMIN.
     * Citizens receive null
     */
    private String changedByName;

    private String remarks;
    private LocalDateTime changedAt;
}