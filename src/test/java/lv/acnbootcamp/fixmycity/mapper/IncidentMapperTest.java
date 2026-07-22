package lv.acnbootcamp.fixmycity.mapper;

import lv.acnbootcamp.fixmycity.dto.incident.AttachmentResponse;
import lv.acnbootcamp.fixmycity.dto.incident.CommentResponse;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.entity.*;
import lv.acnbootcamp.fixmycity.entity.incident.Attachment;
import lv.acnbootcamp.fixmycity.entity.incident.Comment;
import lv.acnbootcamp.fixmycity.entity.incident.Incident;
import lv.acnbootcamp.fixmycity.entity.incident.IncidentPriority;
import lv.acnbootcamp.fixmycity.entity.incident.IncidentStatus;
import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;
import lv.acnbootcamp.fixmycity.util.UserTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class IncidentMapperTest {

    private IncidentMapper incidentMapper;

    @BeforeEach
    void setUp() {
        incidentMapper = new IncidentMapper();
    }

    @Test
    void shouldMapIncidentToResponse() {

        // given
        User citizen = UserTestDataBuilder.aUser()
                .withId(100L)
                .withFullName("John Doe")
                .build();

        Category category = Category.builder()
                .categoryId(10L)
                .name("Infrastructure")
                .description("Infrastructure issues")
                .build();

        Attachment attachment = Attachment.builder()
                .attachmentId(50L)
                .fileName("street-light.jpg")
                .fileType("image/jpeg")
                .filePath("/uploads/street-light.jpg")
                .build();

        Incident incident = Incident.builder()
                .incidentId(1L)
                .title("Broken street light")
                .description("The light has not worked for several days.")
                .locationAddress("Brīvības iela 12")
                .category(category)
                .citizen(citizen)
                .status(IncidentStatus.NEW)
                .priority(IncidentPriority.MEDIUM)
                .attachments(List.of(attachment))
                .build();

        // when
        IncidentResponse response = incidentMapper.toResponse(incident);

        // then
        assertThat(response).isNotNull();

        assertThat(response.getIncidentId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Broken street light");
        assertThat(response.getDescription())
                .isEqualTo("The light has not worked for several days.");
        assertThat(response.getLocationAddress())
                .isEqualTo("Brīvības iela 12");

        assertThat(response.getCategoryId()).isEqualTo(10L);
        assertThat(response.getCategoryName()).isEqualTo("Infrastructure");

        assertThat(response.getCitizenId()).isEqualTo(100L);
        assertThat(response.getCitizenName()).isEqualTo("John Doe");

        assertThat(response.getStatus()).isEqualTo("NEW");
        assertThat(response.getPriority()).isEqualTo("MEDIUM");

        assertThat(response.getAttachment()).isNotNull();
        assertThat(response.getAttachment().getAttachmentId()).isEqualTo(50L);
        assertThat(response.getAttachment().getFileName()).isEqualTo("street-light.jpg");
        assertThat(response.getAttachment().getFileType()).isEqualTo("image/jpeg");
        assertThat(response.getAttachment().getFilePath())
                .isEqualTo("/uploads/street-light.jpg");
    }

    @Test
    void shouldReturnNullWhenIncidentIsNull() {

        // when
        IncidentResponse response = incidentMapper.toResponse(null);

        // then
        assertThat(response).isNull();
    }

    @Test
    void shouldReturnNullAttachmentWhenIncidentHasNoAttachments() {

        User citizen = UserTestDataBuilder.aUser()
                .withId(100L)
                .withFullName("John Doe")
                .build();

        Category category = Category.builder()
                .categoryId(10L)
                .name("Infrastructure")
                .description("Infrastructure issues")
                .build();

        Incident incident = Incident.builder()
                .incidentId(1L)
                .title("Broken street light")
                .description("The light has not worked for several days.")
                .locationAddress("Brīvības iela 12")
                .category(category)
                .citizen(citizen)
                .status(IncidentStatus.NEW)
                .priority(IncidentPriority.MEDIUM)
                .build();

        IncidentResponse response = incidentMapper.toResponse(incident);

        assertThat(response).isNotNull();
        assertThat(response.getAttachment()).isNull();
    }

    @Test
    void shouldReturnNullWhenAttachmentIsNull() {

        AttachmentResponse response = incidentMapper.toAttachmentResponse(null);

        assertThat(response).isNull();
    }

    @Test
    void shouldMapAttachmentToResponse() {

        Attachment attachment = Attachment.builder()
                .attachmentId(50L)
                .fileName("street-light.jpg")
                .fileType("image/jpeg")
                .filePath("/uploads/street-light.jpg")
                .build();

        AttachmentResponse response = incidentMapper.toAttachmentResponse(attachment);

        assertThat(response).isNotNull();
        assertThat(response.getAttachmentId()).isEqualTo(50L);
        assertThat(response.getFileName()).isEqualTo("street-light.jpg");
        assertThat(response.getFileType()).isEqualTo("image/jpeg");
        assertThat(response.getFilePath()).isEqualTo("/uploads/street-light.jpg");
    }

    @Test
    void shouldMapOnlyFirstAttachmentWhenIncidentHasMultipleAttachments() {

        User citizen = UserTestDataBuilder.aUser()
                .withId(100L)
                .withFullName("John Doe")
                .build();

        Category category = Category.builder()
                .categoryId(10L)
                .name("Infrastructure")
                .description("Infrastructure issues")
                .build();

        Attachment firstAttachment = Attachment.builder()
                .attachmentId(1L)
                .fileName("first.jpg")
                .fileType("image/jpeg")
                .filePath("/uploads/first.jpg")
                .build();

        Attachment secondAttachment = Attachment.builder()
                .attachmentId(2L)
                .fileName("second.jpg")
                .fileType("image/jpeg")
                .filePath("/uploads/second.jpg")
                .build();

        Incident incident = Incident.builder()
                .incidentId(1L)
                .title("Broken street light")
                .description("The light has not worked for several days.")
                .locationAddress("Brīvības iela 12")
                .category(category)
                .citizen(citizen)
                .status(IncidentStatus.NEW)
                .priority(IncidentPriority.MEDIUM)
                .attachments(List.of(firstAttachment, secondAttachment))
                .build();

        IncidentResponse response = incidentMapper.toResponse(incident);

        assertThat(response.getAttachment()).isNotNull();
        assertThat(response.getAttachment().getAttachmentId()).isEqualTo(1L);
        assertThat(response.getAttachment().getFileName()).isEqualTo("first.jpg");
    }

    @Test
    void shouldMapCommentToResponse() {
        User author = UserTestDataBuilder.aUser()
                .withId(50L)
                .withFullName("Jane Smith")
                .withRole(Role.COMPANY)
                .build();

        Incident incident = Incident.builder()
                .incidentId(101L)
                .build();

        Comment comment = Comment.builder()
                .commentId(7L)
                .incident(incident)
                .user(author)
                .comment("Replaced the light fixture.")
                .build();
        comment.setCreatedAt(LocalDateTime.of(2026, 7, 18, 15, 55));

        CommentResponse response = incidentMapper.toCommentResponse(comment);

        assertThat(response).isNotNull();
        assertThat(response.getCommentId()).isEqualTo(7L);
        assertThat(response.getIncidentId()).isEqualTo(101L);
        assertThat(response.getComment()).isEqualTo("Replaced the light fixture.");
        assertThat(response.getAuthorName()).isEqualTo("Jane Smith");
        assertThat(response.getAuthorRole()).isEqualTo("COMPANY");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 7, 18, 15, 55));
    }

    @Test
    void shouldReturnNullWhenCommentIsNull() {
        CommentResponse response = incidentMapper.toCommentResponse(null);

        assertThat(response).isNull();
    }

    @Test
    void shouldMapCommentWithNullUserGracefully() {
        Incident incident = Incident.builder().incidentId(5L).build();
        Comment comment = Comment.builder()
                .commentId(1L)
                .incident(incident)
                .user(null)
                .comment("Anonymous note")
                .build();

        CommentResponse response = incidentMapper.toCommentResponse(comment);

        assertThat(response).isNotNull();
        assertThat(response.getAuthorName()).isNull();
        assertThat(response.getAuthorRole()).isNull();
        assertThat(response.getComment()).isEqualTo("Anonymous note");
    }
}

