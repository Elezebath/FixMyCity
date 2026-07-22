package lv.acnbootcamp.fixmycity.security;

import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;
import lv.acnbootcamp.fixmycity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldLoadUserByEmail() {

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.CITIZEN);
        user.setEnabled(true);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                userDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(result);

        assertTrue(result instanceof UserDetailsImpl);

        assertEquals(
                "test@example.com",
                result.getUsername()
        );

        verify(userRepository)
                .findByEmail("test@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotFound() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception =
                assertThrows(
                        UsernameNotFoundException.class,
                        () -> userDetailsService.loadUserByUsername("test@example.com")
                );

        assertEquals(
                "User not found with email: test@example.com",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail("test@example.com");
    }
}