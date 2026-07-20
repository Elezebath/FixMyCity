package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.incident.IncidentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentStatusHistoryRepository extends JpaRepository<IncidentStatusHistory, Long> {
    List<IncidentStatusHistory> findAllByIncident_IncidentIdOrderByChangedAtAsc(Long incidentId);
}
