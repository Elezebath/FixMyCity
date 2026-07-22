package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;
import lv.acnbootcamp.fixmycity.util.UserTestDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    private final UserRepository userRepository;

    @Autowired
    UserRepositoryTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    void findByEmail_returnsUser_whenEmailExists() {
        // given
        User user = UserTestDataBuilder.aUser().withEmail("find.me@example.com").build();
        userRepository.save(user);

        // when
        var found = userRepository.findByEmail("find.me@example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Test User");
    }

    @Test
    void findByEmail_returnsEmpty_whenEmailDoesNotExist() {
        var found = userRepository.findByEmail("nobody@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_returnsTrue_whenEmailAlreadyTaken() {
        userRepository.save(UserTestDataBuilder.aUser().withEmail("taken@example.com").build());

        assertThat(userRepository.existsByEmail("taken@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("free@example.com")).isFalse();
    }

    @Test
    void save_persistsUserWithCorrectRole() {
        User admin = UserTestDataBuilder.aUser().withRole(Role.ADMIN).withEmail("admin@example.com").build();

        User saved = userRepository.save(admin);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
        assertThat(saved.getCreatedAt()).isNotNull(); // set by @PrePersist
    }
}