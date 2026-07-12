package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.RegisterRequest;
import lv.acnbootcamp.fixmycity.dto.UserResponse;
import lv.acnbootcamp.fixmycity.entity.Role;
import lv.acnbootcamp.fixmycity.entity.User;
import lv.acnbootcamp.fixmycity.exception.EmailAlreadyExistsException;
import lv.acnbootcamp.fixmycity.repository.UserRepository;
import lv.acnbootcamp.fixmycity.util.UserTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void register_savesNewUser_whenEmailNotTaken() {
        // given
        RegisterRequest request = new RegisterRequest(
                "new.user@example.com", "password123", "New User", Role.CITIZEN);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");

        User savedUser = UserTestDataBuilder.aUser()
                .withId(1L)
                .withEmail(request.email())
                .withPassword("hashed-password")
                .withFullName(request.fullName())
                .withRole(request.role())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        UserResponse response = authService.register(request);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("new.user@example.com");
        assertThat(response.role()).isEqualTo(Role.CITIZEN);

        // verify the password was hashed before being persisted, never stored raw
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo("hashed-password");
    }

    @Test
    void register_throwsException_whenEmailAlreadyExists() {
        // given
        RegisterRequest request = new RegisterRequest(
                "existing@example.com", "password123", "Existing User", Role.CITIZEN);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("existing@example.com");

        // save() should never be reached if the email check fails fast
        verify(userRepository, never()).save(any());
    }
}