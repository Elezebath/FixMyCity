package lv.acnbootcamp.fixmycity.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lv.acnbootcamp.fixmycity.dto.incident.AssignIncidentRequest;
import lv.acnbootcamp.fixmycity.dto.incident.CreateIncidentRequest;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.dto.incident.ResolveIncidentRequest;
import lv.acnbootcamp.fixmycity.entity.*;
import lv.acnbootcamp.fixmycity.exception.incident.IncidentNotFoundException;
import lv.acnbootcamp.fixmycity.exception.incident.InvalidIncidentException;
import lv.acnbootcamp.fixmycity.exception.incident.InvalidPriorityException;
import lv.acnbootcamp.fixmycity.exception.incident.InvalidStatusException;
import lv.acnbootcamp.fixmycity.exception.category.CategoryNotFoundException;
import lv.acnbootcamp.fixmycity.exception.user.CompanyNotFoundException;
import lv.acnbootcamp.fixmycity.exception.user.UserNotFoundException;
import lv.acnbootcamp.fixmycity.mapper.IncidentMapper;
import lv.acnbootcamp.fixmycity.repository.*;
import lv.acnbootcamp.fixmycity.service.IncidentService;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final CategoryRepository categoryRepository;
    private final IncidentMapper incidentMapper;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CommentRepository commentRepository;

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
        if (status == null) {
            throw new InvalidStatusException("Status cannot be null");
        }
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
        if (!userRepository.existsById(citizenId)) {
            throw new UserNotFoundException("User not found with id: " + citizenId);
        }
        log.info("Fetching incidents for user {}", citizenId);

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

        if (!companyRepository.existsById(companyId)) {
            throw new CompanyNotFoundException("Company not found with id: " + companyId);
        }

        return incidentRepository
                .findAllBySoftDeletedFalseAndAssignedCompany_CompanyId(companyId)
                .stream()
                .map(incidentMapper::toResponse)
                .toList();
    }

    /**
     * Find incidents by category
     */
    public List<IncidentResponse> findAllByCategory(Long categoryId) {
        validateId(categoryId);

        // Check if category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Category not found with id: " + categoryId);
        }

        log.info("Fetching incidents category {}", categoryId);
        return incidentRepository
                .findAllBySoftDeletedFalseAndCategory_CategoryId(categoryId)
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
                .orElseThrow(() -> new IncidentNotFoundException(
                        "Incident not found with id: " + id));

        return incidentMapper.toResponse(incident);
    }

    /**
     * Create new incident
     */
    @Transactional
    public IncidentResponse create(CreateIncidentRequest request) {
        validateRequest(request);

        log.info("Creating new incident for category {}", request.getCategoryId());

        User citizen = userRepository.findById(request.getCitizenId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + request.getCitizenId()));

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Incident incident = Incident.builder()
                .title(request.getTitle()).description(request.getDescription())
                .locationAddress(request.getLocationAddress())
                .category(category)
                .status(IncidentStatus.NEW)
                .citizen(citizen)
                .priority(IncidentPriority.MEDIUM)
                .softDeleted(false)
                .build();

        Incident savedIncident = incidentRepository.save(incident);
        log.info("Incident created successfully with id {}", savedIncident.getIncidentId());

        return incidentMapper.toResponse(savedIncident);
    }

    @Transactional
    public IncidentResponse assignToCompany(Long incidentId, AssignIncidentRequest request) {
        validateId(incidentId);

        if (request == null || request.getCompanyId() == null) {
            throw new InvalidIncidentException("Company ID is required");
        }

        Incident incident = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException(
                        "Incident not found with id: " + incidentId));

        Company company = companyRepository
                .findById(request.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException(
                        "Company not found with id: " + request.getCompanyId()));

        incident.setAssignedCompany(company);
        incident.setStatus(IncidentStatus.ASSIGNED);

        Incident savedIncident = incidentRepository.save(incident);
        log.info("Incident {} assigned to company {}", incidentId, request.getCompanyId());

        return incidentMapper.toResponse(savedIncident);
    }

    @Transactional
    public IncidentResponse resolveByCompany(Long incidentId, ResolveIncidentRequest request, String resolvedByEmail) {
        validateId(incidentId);

        if (request == null || !StringUtils.hasText(request.getComment())) {
            throw new InvalidIncidentException("Comment is required to resolve an incident");
        }

        Incident incident = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException(
                        "Incident not found with id: " + incidentId));

        User resolvedBy = userRepository.findByEmail(resolvedByEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + resolvedByEmail));

        if (incident.getAssignedCompany() == null
                || resolvedBy.getCompany() == null
                || !incident.getAssignedCompany().getCompanyId().equals(resolvedBy.getCompany().getCompanyId())) {
            throw new InvalidIncidentException("Only the assigned company can resolve this incident");
        }

        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setResolvedAt(LocalDateTime.now());
        incidentRepository.save(incident);

        Comment comment = Comment.builder()
                .incident(incident)
                .user(resolvedBy)
                .comment(request.getComment())
                .build();

        commentRepository.save(comment);

        log.info("Incident {} resolved by {}", incidentId, resolvedByEmail);

        return incidentMapper.toResponse(incident);
    }

    private void validateRequest(CreateIncidentRequest request) {

        if (request == null) {
            throw new InvalidIncidentException("Incident request cannot be null");
        }

        if (!StringUtils.hasText(request.getTitle())) {
            throw new InvalidIncidentException("Title is required");
        }

        if (!StringUtils.hasText(request.getDescription())) {
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

}