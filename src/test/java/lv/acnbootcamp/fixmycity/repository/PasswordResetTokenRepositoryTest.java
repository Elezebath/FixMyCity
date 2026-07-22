package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.user.PasswordResetToken;
import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PasswordResetTokenRepositoryTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2024, Month.JANUARY, 10, 12, 0);

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TestEntityManager entityManager;

    @Autowired
    public PasswordResetTokenRepositoryTest(PasswordResetTokenRepository passwordResetTokenRepository,
                                            TestEntityManager entityManager) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.entityManager = entityManager;
    }

    private User user;
    private User otherUser;

    @BeforeEach
    void setUp() {
        user = entityManager.persistAndFlush(buildUser("user@fixmycity.lv"));
        otherUser = entityManager.persistAndFlush(buildUser("other-user@fixmycity.lv"));
    }

    private User buildUser(String email) {
        return User.builder()
                .email(email)
                .password("hashedPassword")
                .fullName("Test User")
                .role(Role.CITIZEN)
                .enabled(true)
                .build();
    }

    private PasswordResetToken buildToken(User owner, String tokenHash, boolean used) {
        return PasswordResetToken.builder()
                .user(owner)
                .tokenHash(tokenHash)
                .expiresAt(BASE_TIME.plusHours(1))
                .used(used)
                .build();
    }

    @Test
    void findByTokenHash_shouldReturnTokenWhenExists() {
        passwordResetTokenRepository.save(buildToken(user, "hash-abc123", false));

        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByTokenHash("hash-abc123");

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(token -> {
                    assertThat(token.getUser().getId()).isEqualTo(user.getId());
                    assertThat(token.isUsed()).isFalse();
                });
    }

    @Test
    void findByTokenHash_shouldReturnEmptyWhenNotFound() {
        Optional<PasswordResetToken> result = passwordResetTokenRepository.findByTokenHash("nonexistent-hash");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteAllByUser_shouldRemoveOnlyTokensBelongingToThatUser() {
        passwordResetTokenRepository.save(buildToken(user, "hash-user-1", false));
        passwordResetTokenRepository.save(buildToken(user, "hash-user-2", true));
        passwordResetTokenRepository.save(buildToken(otherUser, "hash-other-1", false));

        passwordResetTokenRepository.deleteAllByUser(user);

        List<PasswordResetToken> remaining = passwordResetTokenRepository.findAll();
        assertThat(remaining)
                .hasSize(1)
                .extracting(PasswordResetToken::getTokenHash)
                .containsExactly("hash-other-1");
    }

    @Test
    void deleteAllByUser_shouldDoNothingWhenUserHasNoTokens() {
        passwordResetTokenRepository.save(buildToken(otherUser, "hash-other-1", false));

        passwordResetTokenRepository.deleteAllByUser(user);

        assertThat(passwordResetTokenRepository.findAll()).hasSize(1);
    }
}