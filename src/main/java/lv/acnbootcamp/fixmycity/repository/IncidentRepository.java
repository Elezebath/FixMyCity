package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.Incident;
import lv.acnbootcamp.fixmycity.entity.IncidentPriority;
import lv.acnbootcamp.fixmycity.entity.IncidentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findAllBySoftDeletedFalse();
    List<Incident> findAllBySoftDeletedFalseAndStatus(IncidentStatus status);
    List<Incident> findAllBySoftDeletedFalseAndCategory_CategoryId(Long categoryId);
    List<Incident> findAllBySoftDeletedFalseAndAssignedCompany_CompanyId(Long companyId);
    List<Incident> findAllBySoftDeletedFalseAndCitizenId(Long citizenId);
    List<Incident> findAllBySoftDeletedFalseAndPriority(IncidentPriority priority);
    Optional<Incident> findByIncidentIdAndSoftDeletedFalse(Long id);
    boolean existsBySoftDeletedFalseAndCategory_CategoryId(Long categoryId);
}
