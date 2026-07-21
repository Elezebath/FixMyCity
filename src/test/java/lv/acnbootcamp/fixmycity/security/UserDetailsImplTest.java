package lv.acnbootcamp.fixmycity.security;

import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    private User user;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);

        userDetails = new UserDetailsImpl(user);
    }

    @Test
    void shouldReturnEmailAsUsername() {

        assertEquals(
                "test@example.com",
                userDetails.getUsername()
        );
    }

    @Test
    void shouldReturnPassword() {

        assertEquals(
                "password",
                userDetails.getPassword()
        );
    }

    @Test
    void shouldReturnEnabledStatus() {

        assertTrue(userDetails.isEnabled());
    }

    @Test
    void shouldReturnDisabledStatus() {

        user.setEnabled(false);

        assertFalse(userDetails.isEnabled());
    }

    @Test
    void shouldReturnRoleAsGrantedAuthority() {

        Collection<? extends GrantedAuthority> authorities =
                userDetails.getAuthorities();

        assertEquals(1, authorities.size());

        GrantedAuthority authority = authorities.iterator().next();

        assertEquals(
                "ROLE_ADMIN",
                authority.getAuthority()
        );
    }

    @Test
    void shouldReturnWrappedUser() {

        assertSame(
                user,
                userDetails.getUser()
        );
    }
}