package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.incident.AssignIncidentRequest;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.dto.incident.CreateIncidentRequest;
import lv.acnbootcamp.fixmycity.dto.incident.ResolveIncidentRequest;
import lv.acnbootcamp.fixmycity.entity.incident.IncidentPriority;
import lv.acnbootcamp.fixmycity.entity.incident.IncidentStatus;

import java.util.List;

public interface IncidentService {
    List<IncidentResponse> findAll();
    IncidentResponse findById(Long id);
    IncidentResponse create(CreateIncidentRequest request, Long citizenId);
    List<IncidentResponse> findAllByPriority(IncidentPriority priority);
    List<IncidentResponse> findAllByCategory(Long categoryId);
    List<IncidentResponse> findAllByCompany(Long companyId);
    List<IncidentResponse> findAllByCitizen(Long citizenId);
    List<IncidentResponse> findAllByStatus(IncidentStatus status);
    IncidentResponse assignToCompany(Long incidentId, AssignIncidentRequest request);
    IncidentResponse resolveByCompany(Long incidentId, ResolveIncidentRequest request, String resolvedByEmail);
}

