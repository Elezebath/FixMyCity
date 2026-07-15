package lv.acnbootcamp.fixmycity.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lv.acnbootcamp.fixmycity.dto.incident.CreateIncidentRequest;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.entity.IncidentPriority;
import lv.acnbootcamp.fixmycity.entity.IncidentStatus;
import lv.acnbootcamp.fixmycity.service.IncidentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(
        name = "Incident Management",
        description = "APIs for reporting and managing city incidents"
)
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @Operation(
            summary = "Get all incidents",
            description = "Returns all active incidents"
    )
    @ApiResponse(responseCode = "200", description = "Incidents retrieved successfully")
    public List<IncidentResponse> findAll() {

        log.info("REST request to fetch all incidents");

        return incidentService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get incident by ID",
            description = "Returns incident details"
    )
    @ApiResponse(responseCode = "200", description = "Incident found")
    @ApiResponse(responseCode = "404", description = "Incident not found")
    public IncidentResponse findById(@Parameter(description = "Incident ID", example = "1") @PathVariable @Positive(message = "Incident ID must be positive") Long id) {

        log.info("REST request to fetch incident {}", id);
        return incidentService.findById(id);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get incidents by status")
    @ApiResponse(responseCode = "200", description = "Incidents retrieved successfully")
    public List<IncidentResponse> findByStatus(@PathVariable IncidentStatus status) {
        return incidentService.findAllByStatus(status);
    }

    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get incidents by priority")
    @ApiResponse(responseCode = "200", description = "Incidents retrieved successfully")
    public List<IncidentResponse> findByPriority(
            @PathVariable IncidentPriority priority) {

        return incidentService.findAllByPriority(priority);
    }

    @GetMapping("/citizen/{citizenId}")
    @Operation(summary = "Get incidents reported by citizen")
    @ApiResponse(responseCode = "200", description = "Incidents retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid citizen ID")
    public List<IncidentResponse> findByCitizen(
            @PathVariable
            @Positive
            Long citizenId) {

        return incidentService.findAllByCitizen(citizenId);
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get incidents assigned to company")
    @ApiResponse(responseCode = "200", description = "Incidents retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid company ID")
    public List<IncidentResponse> findByCompany(
            @PathVariable
            @Positive
            Long companyId) {

        return incidentService.findAllByCompany(companyId);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get incidents by category")
    @ApiResponse(responseCode = "200", description = "Incidents retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid category ID")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public List<IncidentResponse> findByCategory(
            @PathVariable
            @Positive
            Long categoryId) {

        return incidentService.findAllByCategory(categoryId);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('CITIZEN', 'MANAGER', 'ADMIN')")
    @Operation(
            summary = "Report a new incident",
            description = "Creates a new incident with category, location and optional photo."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Incident created successfully"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation failed",
            content = @Content(
                    schema = @Schema()
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Unauthorized"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Category not found"
    )
    @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error"
    )
    public IncidentResponse create(@Valid @ModelAttribute CreateIncidentRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("REST request to create incident with title '{}', authenticated user: {}",
                request.getTitle(), 
                auth != null ? auth.getName() : "anonymous");
        return incidentService.create(request);
    }
}
