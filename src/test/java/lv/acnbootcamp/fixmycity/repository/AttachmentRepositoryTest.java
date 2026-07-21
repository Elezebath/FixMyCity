package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.Category;
import lv.acnbootcamp.fixmycity.entity.incident.Attachment;
import lv.acnbootcamp.fixmycity.entity.incident.Incident;
import lv.acnbootcamp.fixmycity.entity.incident.IncidentPriority;
import lv.acnbootcamp.fixmycity.entity.incident.IncidentStatus;
import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AttachmentRepositoryTest {

    private final AttachmentRepository attachmentRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public AttachmentRepositoryTest(
            AttachmentRepository attachmentRepository,
            IncidentRepository incidentRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {
        this.attachmentRepository = attachmentRepository;
        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    private Incident testIncident;

    @BeforeEach
    void setUp() {
        User citizen = User.builder()
                .email("citizen@example.com")
                .password("password")
                .fullName("Test Citizen")
                .role(Role.CITIZEN)
                .enabled(true)
                .build();
        citizen = userRepository.save(citizen);

        Category category = Category.builder()
                .name("Roads")
                .description("Road-related incidents")
                .isDeleted(false)
                .build();
        category = categoryRepository.save(category);

        Incident incident = Incident.builder()
                .citizen(citizen)
                .category(category)
                .title("Test incident")
                .description("Test description")
                .locationAddress("Test address")
                .priority(IncidentPriority.MEDIUM)
                .status(IncidentStatus.NEW)
                .softDeleted(false)
                .build();

        testIncident = incidentRepository.save(incident);
    }

    private Attachment buildAttachment() {
        return Attachment.builder()
                .incident(testIncident)
                .fileName("test-photo.jpg")
                .filePath("/uploads/test-photo.jpg")
                .fileType("image/jpeg")
                .build();
    }

    @Test
    void shouldSaveAttachment() {
        Attachment saved = attachmentRepository.save(buildAttachment());

        assertNotNull(saved.getAttachmentId());
    }

    @Test
    void shouldFindAttachmentById() {
        Attachment saved = attachmentRepository.save(buildAttachment());

        Optional<Attachment> found = attachmentRepository.findById(saved.getAttachmentId());

        assertTrue(found.isPresent());
        assertEquals(saved.getAttachmentId(), found.get().getAttachmentId());
    }

    @Test
    void shouldReturnEmptyWhenAttachmentNotFound() {
        Optional<Attachment> found = attachmentRepository.findById(999L);

        assertTrue(found.isEmpty());
    }

    @Test
    void shouldDeleteAttachment() {
        Attachment saved = attachmentRepository.save(buildAttachment());

        attachmentRepository.deleteById(saved.getAttachmentId());

        assertFalse(attachmentRepository.findById(saved.getAttachmentId()).isPresent());
    }

    @Test
    void shouldCountAttachments() {
        long before = attachmentRepository.count();

        attachmentRepository.save(buildAttachment());

        long after = attachmentRepository.count();

        assertEquals(before + 1, after);
    }
}