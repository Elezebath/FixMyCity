package lv.acnbootcamp.fixmycity.util;

import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;

/**
 * Fluent builder for constructing User test fixtures with sensible defaults.
 * Lets tests override only the fields relevant to what they're testing,
 * instead of repeating full User.builder()...build() calls everywhere.
 *
 * Usage:
 *   User citizen = UserTestDataBuilder.aUser().build();
 *   User admin = UserTestDataBuilder.aUser().withRole(Role.ADMIN).withEmail("admin@test.com").build();
 */
public class UserTestDataBuilder {

    private Long id = null;
    private String email = "test.user@example.com";
    private String password = "$2a$10$encodedPlaceholderHashValueXXXXXXXXXXXXXXXXXXXXXX"; // pretend BCrypt hash
    private String fullName = "Test User";
    private Role role = Role.CITIZEN;
    private boolean enabled = true;

    private UserTestDataBuilder() {}

    public static UserTestDataBuilder aUser() {
        return new UserTestDataBuilder();
    }

    public UserTestDataBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public UserTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserTestDataBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public UserTestDataBuilder withFullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public UserTestDataBuilder withRole(Role role) {
        this.role = role;
        return this;
    }

    public UserTestDataBuilder disabled() {
        this.enabled = false;
        return this;
    }

    public User build() {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .fullName(fullName)
                .role(role)
                .enabled(enabled)
                .build();
    }
}