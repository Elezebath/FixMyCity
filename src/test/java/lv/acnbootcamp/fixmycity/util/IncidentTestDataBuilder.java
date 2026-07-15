package lv.acnbootcamp.fixmycity.util;

import lv.acnbootcamp.fixmycity.entity.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Fluent builder for constructing Incident test fixtures with sensible defaults.
 * Lets tests override only the fields relevant to what they're testing.
 */
public class IncidentTestDataBuilder {

    private Long incidentId = 1L;
    private User citizen;
    private Category category;
    private Company assignedCompany;
    private String title = "Broken Street Light";
    private String description = "The street light is not working";
    private String locationAddress = "Brivibas Street 12, Riga";
    private IncidentPriority priority = IncidentPriority.MEDIUM;
    private IncidentStatus status = IncidentStatus.NEW;
    private LocalDateTime resolvedAt = null;
    private Boolean softDeleted = false;
    private ArrayList<Attachment> attachments = new ArrayList<>();
    private ArrayList<Comment> comments = new ArrayList<>();
    private ArrayList<IncidentAssignment> assignments = new ArrayList<>();
    private ArrayList<IncidentStatusHistory> statusHistory = new ArrayList<>();

    private IncidentTestDataBuilder() {
        this.citizen = UserTestDataBuilder.aUser().withId(10L).build();
        this.category = CategoryTestDataBuilder.aCategory().withId(1L).build();
    }

    public static IncidentTestDataBuilder anIncident() {
        return new IncidentTestDataBuilder();
    }

    public IncidentTestDataBuilder withIncidentId(Long incidentId) {
        this.incidentId = incidentId;
        return this;
    }

    public IncidentTestDataBuilder withCitizen(User citizen) {
        this.citizen = citizen;
        return this;
    }

    public IncidentTestDataBuilder withCategory(Category category) {
        this.category = category;
        return this;
    }

    public IncidentTestDataBuilder withAssignedCompany(Company assignedCompany) {
        this.assignedCompany = assignedCompany;
        return this;
    }

    public IncidentTestDataBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public IncidentTestDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public IncidentTestDataBuilder withLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
        return this;
    }

    public IncidentTestDataBuilder withPriority(IncidentPriority priority) {
        this.priority = priority;
        return this;
    }

    public IncidentTestDataBuilder withStatus(IncidentStatus status) {
        this.status = status;
        return this;
    }

    public IncidentTestDataBuilder withResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
        return this;
    }

    public IncidentTestDataBuilder withSoftDeleted(Boolean softDeleted) {
        this.softDeleted = softDeleted;
        return this;
    }

    public IncidentTestDataBuilder withAttachments(ArrayList<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public IncidentTestDataBuilder withComments(ArrayList<Comment> comments) {
        this.comments = comments;
        return this;
    }

    public IncidentTestDataBuilder withAssignments(ArrayList<IncidentAssignment> assignments) {
        this.assignments = assignments;
        return this;
    }

    public IncidentTestDataBuilder withStatusHistory(ArrayList<IncidentStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
        return this;
    }

    public Incident build() {
        return Incident.builder()
                .incidentId(incidentId)
                .citizen(citizen)
                .category(category)
                .assignedCompany(assignedCompany)
                .title(title)
                .description(description)
                .locationAddress(locationAddress)
                .priority(priority)
                .status(status)
                .resolvedAt(resolvedAt)
                .softDeleted(softDeleted)
                .attachments(attachments)
                .comments(comments)
                .assignments(assignments)
                .statusHistory(statusHistory)
                .build();
    }
}