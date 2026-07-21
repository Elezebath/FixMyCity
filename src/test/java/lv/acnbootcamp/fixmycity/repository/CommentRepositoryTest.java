package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.Category;
import lv.acnbootcamp.fixmycity.entity.incident.Comment;
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

import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryTest {

    private final CommentRepository commentRepository;
    private final TestEntityManager entityManager;

    @Autowired
    public CommentRepositoryTest(CommentRepository commentRepository, TestEntityManager entityManager) {
        this.commentRepository = commentRepository;
        this.entityManager = entityManager;
    }

    private User citizen;
    private Incident incident;

    @BeforeEach
    void setUp() {
        User rawUser = User.builder()
                .email("citizen@fixmycity.lv")
                .password("hashedPassword")
                .fullName("Jane Citizen")
                .role(Role.CITIZEN)
                .enabled(true)
                .build();
        citizen = entityManager.persistAndFlush(rawUser);

        Category rawCategory = Category.builder()
                .name("Roads")
                .description("Road related issues")
                .isDeleted(false)
                .build();
        Category category = entityManager.persistAndFlush(rawCategory);

        Incident rawIncident = Incident.builder()
                .citizen(citizen)
                .category(category)
                .title("Pothole on Main Street")
                .description("Large pothole causing traffic issues")
                .locationAddress("Main Street 1")
                .priority(IncidentPriority.MEDIUM)
                .status(IncidentStatus.NEW)
                .softDeleted(false)
                .build();
        incident = entityManager.persistAndFlush(rawIncident);
    }

    private Comment buildComment(String text) {
        return Comment.builder()
                .incident(incident)
                .user(citizen)
                .comment(text)
                .build();
    }

    @Test
    void save_shouldPersistCommentAndGenerateId() {
        Comment comment = buildComment("This needs urgent attention");

        Comment saved = commentRepository.save(comment);

        assertThat(saved.getCommentId()).isNotNull();
    }

    @Test
    void findById_shouldReturnPersistedComment() {
        Comment saved = commentRepository.save(buildComment("Thanks for reporting"));

        Optional<Comment> result = commentRepository.findById(saved.getCommentId());

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getComment()).isEqualTo("Thanks for reporting");
                    assertThat(found.getUser().getId()).isEqualTo(citizen.getId());
                    assertThat(found.getIncident().getIncidentId()).isEqualTo(incident.getIncidentId());
                });
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        Optional<Comment> result = commentRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllPersistedComments() {
        commentRepository.save(buildComment("First comment"));
        commentRepository.save(buildComment("Second comment"));

        List<Comment> result = commentRepository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteById_shouldRemoveComment() {
        Comment saved = commentRepository.save(buildComment("Temporary comment"));

        commentRepository.deleteById(saved.getCommentId());

        assertThat(commentRepository.findById(saved.getCommentId())).isEmpty();
    }

    @Test
    void existsById_shouldReturnTrueForPersistedComment() {
        Comment saved = commentRepository.save(buildComment("Existing comment"));

        boolean exists = commentRepository.existsById(saved.getCommentId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsById_shouldReturnFalseForNonExistentComment() {
        boolean exists = commentRepository.existsById(999L);

        assertThat(exists).isFalse();
    }
}