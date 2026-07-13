package lv.acnbootcamp.fixmycity.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lv.acnbootcamp.fixmycity.dto.incident.CreateIncidentRequest;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.entity.Incident;
import lv.acnbootcamp.fixmycity.entity.IncidentPriority;
import lv.acnbootcamp.fixmycity.entity.IncidentStatus;
import lv.acnbootcamp.fixmycity.entity.Category;
import lv.acnbootcamp.fixmycity.exception.*;
import lv.acnbootcamp.fixmycity.mapper.IncidentMapper;
import lv.acnbootcamp.fixmycity.repository.CategoryRepository;
import lv.acnbootcamp.fixmycity.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IncidentServiceImpl {

    private final IncidentRepository incidentRepository;
    private final CategoryRepository categoryRepository;
    private final IncidentMapper incidentMapper;

    /**
     * Find all active incidents
     */
    public List<IncidentResponse> findAll() {
        log.info("Fetching all active incidents");

        return incidentRepository
                .findAllBySoftDeletedFalse()
                .stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    /**
     * Find incidents by status
     */
    public List<IncidentResponse> findAllByStatus(IncidentStatus status) {
        validateStatus(status);
        log.info(
                "Fetching incidents with status {}",
                status
        );

        return incidentRepository
                .findAllBySoftDeletedFalseAndStatus(status)
                .stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    /**
     * Find incidents reported by citizen
     */
    public List<IncidentResponse> findAllByCitizen(Long citizenId) {

        validateId(citizenId);
        log.info("Fetching incidents for citizen {}", citizenId);

        return incidentRepository
                .findAllBySoftDeletedFalseAndCitizenId(citizenId)
                .stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    /**
     * Find incidents assigned to company
     */
    public List<IncidentResponse> findAllByCompany(Long companyId) {
        validateId(companyId);
        log.info("Fetching incidents for company {}", companyId);

        return incidentRepository
                .findAllBySoftDeletedFalseAndAssignedCompanyId(companyId)
                .stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    /**
     * Find incidents by category
     */
    public List<IncidentResponse> findAllByCategory(Long categoryId) {
        validateId(categoryId);

        log.info("Fetching incidents category {}", categoryId);
        return incidentRepository
                .findAllBySoftDeletedFalseAndCategoryId(categoryId)
                .stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    /**
     * Find incidents by priority
     */
    public List<IncidentResponse> findAllByPriority(IncidentPriority priority) {
        if (priority == null) {
            throw new InvalidPriorityException("Priority cannot be null");
        }

        log.info("Fetching incidents priority {}", priority);
        return incidentRepository
                .findAllBySoftDeletedFalseAndPriority(priority)
                .stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    /**
     * Find incident by id
     */
    public IncidentResponse findById(Long id) {
        validateId(id);
        log.info("Finding incident {}", id);

        Incident incident = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(id)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found with id: " + id));

        return incidentMapper.toResponse(incident);
    }

    /**
     * Create new incident
     */
    @Transactional
    public IncidentResponse create(CreateIncidentRequest request) {
        validateRequest(request);

        log.info("Creating new incident for category {}", request.getCategoryId());

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Incident incident = Incident.builder()
                .title(request.getTitle()).description(request.getDescription())
                .locationAddress(request.getLocationAddress())
                .category(category)
                .status(IncidentStatus.NEW)
                .priority(IncidentPriority.MEDIUM)
                .softDeleted(false)
                .build();

        Incident savedIncident = incidentRepository.save(incident);
        log.info("Incident created successfully with id {}", savedIncident.getIncidentId());

        return incidentMapper.toResponse(savedIncident);
    }

    private void validateRequest(CreateIncidentRequest request) {

        if (request == null) {
            throw new InvalidIncidentException("Incident request cannot be null");
        }

        if (!StringUtils.hasText(request.getTitle())) {
            throw new InvalidIncidentException("Title is required");
        }

        if (!StringUtils.hasText(
                request.getDescription())) {

            throw new InvalidIncidentException("Description is required");
        }

        if (request.getCategoryId() == null) {
            throw new InvalidIncidentException("Category is required");
        }

    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid id");
        }
    }

    private void validateStatus(IncidentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }
}