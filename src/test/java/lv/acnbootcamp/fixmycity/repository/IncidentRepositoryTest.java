package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.*;
import lv.acnbootcamp.fixmycity.util.AttachmentTestDataBuilder;
import lv.acnbootcamp.fixmycity.util.CategoryTestDataBuilder;
import lv.acnbootcamp.fixmycity.util.IncidentTestDataBuilder;
import lv.acnbootcamp.fixmycity.util.UserTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IncidentRepositoryTest {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    private User testCitizen;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCitizen = UserTestDataBuilder.aUser()
                .withId(null)
                .withEmail("citizen@test.com")
                .withRole(Role.CITIZEN)
                .build();
        userRepository.save(testCitizen);

        testCategory = CategoryTestDataBuilder.aCategory()
                .withId(null)
                .withName("Test Category")
                .withDescription("Test Description")
                .build();
        categoryRepository.save(testCategory);
    }

    @Test
    void findAllBySoftDeletedFalse_returnsAllNonDeletedIncidents() {
        Incident incident1 = createAndSaveIncident("Incident 1", IncidentStatus.NEW);
        Incident incident2 = createAndSaveIncident("Incident 2", IncidentStatus.IN_PROGRESS);

        List<Incident> incidents = incidentRepository.findAllBySoftDeletedFalse();

        assertThat(incidents).hasSize(2);
        assertThat(incidents).extracting(Incident::getTitle)
                .containsExactlyInAnyOrder("Incident 1", "Incident 2");
    }

    @Test
    void findAllBySoftDeletedFalse_excludesSoftDeletedIncidents() {
        createAndSaveIncident("Active Incident", IncidentStatus.NEW);
        Incident deletedIncident = IncidentTestDataBuilder.anIncident()
                .withIncidentId(null)
                .withTitle("Deleted Incident")
                .withCitizen(testCitizen)
                .withCategory(testCategory)
                .withSoftDeleted(true)
                .build();
        incidentRepository.save(deletedIncident);

        List<Incident> incidents = incidentRepository.findAllBySoftDeletedFalse();

        assertThat(incidents).hasSize(1);
        assertThat(incidents.get(0).getTitle()).isEqualTo("Active Incident");
    }

    @Test
    void findAllBySoftDeletedFalse_returnsEmptyList_whenNoIncidentsExist() {
        List<Incident> incidents = incidentRepository.findAllBySoftDeletedFalse();

        assertThat(incidents).isEmpty();
    }

    @Test
    void findAllBySoftDeletedFalseAndStatus_returnsIncidentsWithSpecificStatus() {
        createAndSaveIncident("New Incident 1", IncidentStatus.NEW);
        createAndSaveIncident("In Progress Incident", IncidentStatus.IN_PROGRESS);
        createAndSaveIncident("New Incident 2", IncidentStatus.NEW);

        List<Incident> newIncidents = incidentRepository
                .findAllBySoftDeletedFalseAndStatus(IncidentStatus.NEW);

        assertThat(newIncidents).hasSize(2);
        assertThat(newIncidents).extracting(Incident::getStatus)
                .containsOnly(IncidentStatus.NEW);
    }

    @Test
    void findAllBySoftDeletedFalseAndStatus_returnsEmptyList_whenNoMatch() {
        createAndSaveIncident("New Incident", IncidentStatus.NEW);

        List<Incident> resolvedIncidents = incidentRepository
                .findAllBySoftDeletedFalseAndStatus(IncidentStatus.RESOLVED);

        assertThat(resolvedIncidents).isEmpty();
    }

    @Test
    void findAllBySoftDeletedFalseAndCitizenId_returnsIncidentsReportedByCitizen() {
        createAndSaveIncident("Citizen's Incident 1", IncidentStatus.NEW);
        createAndSaveIncident("Citizen's Incident 2", IncidentStatus.NEW);

        List<Incident> incidents = incidentRepository
                .findAllBySoftDeletedFalseAndCitizenId(testCitizen.getId());

        assertThat(incidents).hasSize(2);
        assertThat(incidents).extracting(Incident::getCitizen)
                .containsOnly(testCitizen);
    }

    @Test
    void findAllBySoftDeletedFalseAndCitizenId_returnsEmptyList_whenCitizenNotExists() {
        List<Incident> incidents = incidentRepository
                .findAllBySoftDeletedFalseAndCitizenId(999L);

        assertThat(incidents).isEmpty();
    }

    @Test
    void findAllBySoftDeletedFalseAndCategory_CategoryId_returnsIncidentsInCategory() {
        createAndSaveIncident("Category Incident 1", IncidentStatus.NEW);
        createAndSaveIncident("Category Incident 2", IncidentStatus.NEW);

        List<Incident> incidents = incidentRepository
                .findAllBySoftDeletedFalseAndCategory_CategoryId(testCategory.getCategoryId());

        assertThat(incidents).hasSize(2);
        assertThat(incidents).extracting(Incident::getCategory)
                .containsOnly(testCategory);
    }

    @Test
    void findAllBySoftDeletedFalseAndCategory_CategoryId_returnsEmptyList_whenCategoryNotExists() {
        List<Incident> incidents = incidentRepository
                .findAllBySoftDeletedFalseAndCategory_CategoryId(999L);

        assertThat(incidents).isEmpty();
    }

    @Test
    void findAllBySoftDeletedFalseAndPriority_returnsIncidentsWithPriority() {
        createAndSaveIncident("High Priority Incident", IncidentPriority.HIGH);
        createAndSaveIncident("Medium Priority Incident", IncidentPriority.MEDIUM);

        List<Incident> highPriorityIncidents = incidentRepository
                .findAllBySoftDeletedFalseAndPriority(IncidentPriority.HIGH);

        assertThat(highPriorityIncidents).hasSize(1);
        assertThat(highPriorityIncidents.get(0).getPriority())
                .isEqualTo(IncidentPriority.HIGH);
    }

    @Test
    void findAllBySoftDeletedFalseAndPriority_returnsLowPriorityIncidents() {
        createAndSaveIncident("Low Priority Incident", IncidentPriority.LOW);

        List<Incident> lowPriorityIncidents = incidentRepository
                .findAllBySoftDeletedFalseAndPriority(IncidentPriority.LOW);

        assertThat(lowPriorityIncidents).hasSize(1);
        assertThat(lowPriorityIncidents.get(0).getPriority())
                .isEqualTo(IncidentPriority.LOW);
    }

    @Test
    void findByIncidentIdAndSoftDeletedFalse_returnsIncident_whenExistsAndNotDeleted() {
        Incident incident = createAndSaveIncident("Test Incident", IncidentStatus.NEW);

        Optional<Incident> found = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(incident.getIncidentId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Incident");
    }

    @Test
    void findByIncidentIdAndSoftDeletedFalse_returnsEmpty_whenIncidentIsSoftDeleted() {
        Incident deletedIncident = IncidentTestDataBuilder.anIncident()
                .withIncidentId(null)
                .withTitle("Deleted Incident")
                .withCitizen(testCitizen)
                .withCategory(testCategory)
                .withSoftDeleted(true)
                .build();
        incidentRepository.save(deletedIncident);

        Optional<Incident> found = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(deletedIncident.getIncidentId());

        assertThat(found).isEmpty();
    }

    @Test
    void findByIncidentIdAndSoftDeletedFalse_returnsEmpty_whenIncidentNotExists() {
        Optional<Incident> found = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void save_createsIncidentWithSoftDeletedTrue() {
        Incident incident = IncidentTestDataBuilder.anIncident()
                .withIncidentId(null)
                .withTitle("Initially Deleted")
                .withSoftDeleted(true)
                .withCitizen(testCitizen)
                .withCategory(testCategory)
                .build();
        incidentRepository.save(incident);

        Optional<Incident> found = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(incident.getIncidentId());

        assertThat(found).isEmpty();

        Optional<Incident> directlyFound = incidentRepository.findById(incident.getIncidentId());
        assertThat(directlyFound).isPresent();
        assertThat(directlyFound.get().getSoftDeleted()).isTrue();
    }

    @Test
    void save_persistsIncidentWithAttachment() {
        Incident incident = createAndSaveIncident("Incident with Attachment", IncidentStatus.NEW);

        Attachment attachment = AttachmentTestDataBuilder.anAttachment()
                .withAttachmentId(null)
                .withIncident(incident)
                .withFileName("test.jpg")
                .withFilePath("/uploads/test.jpg")
                .withFileType("image/jpeg")
                .build();

        incident.getAttachments().add(attachment);
        incidentRepository.save(incident);

        Optional<Incident> found = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(incident.getIncidentId());
        assertThat(found).isPresent();
        assertThat(found.get().getAttachments()).hasSize(1);
        assertThat(found.get().getAttachments().get(0).getFileName())
                .isEqualTo("test.jpg");
    }

    @Test
    void save_persistsIncidentWithMultipleAttachments() {
        Incident incident = createAndSaveIncident("Incident with Multiple Attachments", IncidentStatus.NEW);

        Attachment attachment1 = AttachmentTestDataBuilder.anAttachment()
                .withAttachmentId(null)
                .withIncident(incident)
                .withFileName("test1.jpg")
                .build();

        Attachment attachment2 = AttachmentTestDataBuilder.anAttachment()
                .withAttachmentId(null)
                .withIncident(incident)
                .withFileName("test2.pdf")
                .withFileType("application/pdf")
                .build();

        incident.getAttachments().add(attachment1);
        incident.getAttachments().add(attachment2);
        incidentRepository.save(incident);

        Optional<Incident> found = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(incident.getIncidentId());
        assertThat(found).isPresent();
        assertThat(found.get().getAttachments()).hasSize(2);
    }

    @Test
    void save_createsIncidentWithSpecificStatus() {

        Incident incident = createAndSaveIncident("Medium Priority Incident", IncidentStatus.IN_PROGRESS);

        Optional<Incident> found = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(incident.getIncidentId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(IncidentStatus.IN_PROGRESS);
    }

    @Test
    void save_createsIncidentWithSpecificPriority() {
        Incident incident = createAndSaveIncident("High Priority Incident", IncidentPriority.HIGH);

        Optional<Incident> found = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(incident.getIncidentId());
        assertThat(found).isPresent();
        assertThat(found.get().getPriority()).isEqualTo(IncidentPriority.HIGH);
    }

    @Test
    void findById_loadsIncidentWithCitizen() {
        Incident incident = createAndSaveIncident("Lazy Load Test", IncidentStatus.NEW);
        incidentRepository.flush();

        Optional<Incident> found = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(incident.getIncidentId());

        assertThat(found).isPresent();
        assertThat(found.get().getCitizen()).isNotNull();
        assertThat(found.get().getCitizen().getEmail()).isEqualTo("citizen@test.com");
    }

    @Test
    void findById_loadsIncidentWithCategory() {
        Incident incident = createAndSaveIncident("Category Lazy Load Test", IncidentStatus.NEW);
        incidentRepository.flush();

        Optional<Incident> found = incidentRepository
                .findByIncidentIdAndSoftDeletedFalse(incident.getIncidentId());

        assertThat(found).isPresent();
        assertThat(found.get().getCategory()).isNotNull();
        assertThat(found.get().getCategory().getName()).isEqualTo("Test Category");
    }

    private Incident createAndSaveIncident(String title, IncidentStatus status) {
        Incident incident = IncidentTestDataBuilder.anIncident()
                .withIncidentId(null)
                .withTitle(title)
                .withStatus(status)
                .withCitizen(testCitizen)
                .withCategory(testCategory)
                .build();
        return incidentRepository.save(incident);
    }

    private Incident createAndSaveIncident(String title, IncidentPriority priority) {
        Incident incident = IncidentTestDataBuilder.anIncident()
                .withIncidentId(null)
                .withTitle(title)
                .withPriority(priority)
                .withCitizen(testCitizen)
                .withCategory(testCategory)
                .build();
        return incidentRepository.save(incident);
    }
}