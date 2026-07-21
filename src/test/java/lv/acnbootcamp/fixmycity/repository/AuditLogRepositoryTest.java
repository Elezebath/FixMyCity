package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.audit.AuditAction;
import lv.acnbootcamp.fixmycity.entity.audit.AuditEntityType;
import lv.acnbootcamp.fixmycity.entity.audit.AuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuditLogRepositoryTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2024, Month.JANUARY, 10, 12, 0);

    private final AuditLogRepository auditLogRepository;

    @Autowired
    public AuditLogRepositoryTest(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    private AuditLog logOldestCategory;
    private AuditLog logMiddleCategory;
    private AuditLog logNewestCategory;
    private AuditLog logUser;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();

        logOldestCategory = buildLog(AuditEntityType.CATEGORY, 1L, BASE_TIME.minusDays(3));
        logMiddleCategory = buildLog(AuditEntityType.CATEGORY, 1L, BASE_TIME.minusDays(2));
        logNewestCategory = buildLog(AuditEntityType.CATEGORY, 2L, BASE_TIME.minusDays(1));
        logUser = buildLog(AuditEntityType.USER, 5L, BASE_TIME);

        auditLogRepository.saveAll(List.of(logOldestCategory, logMiddleCategory, logNewestCategory, logUser));
    }

    private AuditLog buildLog(AuditEntityType entityType, Long entityId, LocalDateTime timestamp) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setTimestamp(timestamp);
        log.setAction(AuditAction.UPDATE);
        log.setPerformedByEmail("test@fixmycity.lv");

        return log;
    }

    @Test
    void findAllByOrderByTimestampDesc_shouldReturnAllLogsNewestFirst() {
        List<AuditLog> result = auditLogRepository.findAllByOrderByTimestampDesc();

        assertThat(result)
                .hasSize(4)
                .containsExactly(logUser, logNewestCategory, logMiddleCategory, logOldestCategory);
    }

    @Test
    void findAllByEntityTypeOrderByTimestampDesc_shouldReturnOnlyMatchingType() {
        List<AuditLog> result = auditLogRepository.findAllByEntityTypeOrderByTimestampDesc(AuditEntityType.CATEGORY);

        assertThat(result)
                .hasSize(3)
                .containsExactly(logNewestCategory, logMiddleCategory, logOldestCategory)
                .allMatch(log -> log.getEntityType() == AuditEntityType.CATEGORY);
    }

    @Test
    void findAllByEntityTypeOrderByTimestampDesc_shouldReturnEmptyListWhenNoMatch() {
        auditLogRepository.deleteAll();
        auditLogRepository.save(buildLog(AuditEntityType.USER, 1L, BASE_TIME));

        List<AuditLog> result = auditLogRepository.findAllByEntityTypeOrderByTimestampDesc(AuditEntityType.CATEGORY);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByEntityIdOrderByTimestampDesc_shouldReturnOnlyMatchingId() {
        List<AuditLog> result = auditLogRepository.findAllByEntityIdOrderByTimestampDesc(1L);

        assertThat(result)
                .hasSize(2)
                .containsExactly(logMiddleCategory, logOldestCategory)
                .allMatch(log -> log.getEntityId().equals(1L));
    }

    @Test
    void findAllByEntityIdOrderByTimestampDesc_shouldReturnEmptyListWhenNoMatch() {
        List<AuditLog> result = auditLogRepository.findAllByEntityIdOrderByTimestampDesc(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByEntityTypeAndEntityIdOrderByTimestampDesc_shouldReturnOnlyMatchingTypeAndId() {
        List<AuditLog> result = auditLogRepository
                .findAllByEntityTypeAndEntityIdOrderByTimestampDesc(AuditEntityType.CATEGORY, 1L);

        assertThat(result)
                .hasSize(2)
                .containsExactly(logMiddleCategory, logOldestCategory);
    }

    @Test
    void findAllByEntityTypeAndEntityIdOrderByTimestampDesc_shouldNotReturnDifferentEntityId() {
        List<AuditLog> result = auditLogRepository
                .findAllByEntityTypeAndEntityIdOrderByTimestampDesc(AuditEntityType.CATEGORY, 2L);

        assertThat(result)
                .hasSize(1)
                .containsExactly(logNewestCategory);
    }

    @Test
    void findAllByEntityTypeAndEntityIdOrderByTimestampDesc_shouldNotReturnDifferentEntityType() {
        List<AuditLog> result = auditLogRepository
                .findAllByEntityTypeAndEntityIdOrderByTimestampDesc(AuditEntityType.USER, 1L);

        assertThat(result).isEmpty();
    }
}