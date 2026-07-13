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
import lv.acnbootcamp.fixmycity.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import lv.acnbootcamp.fixmycity.dto.LoginRequest;
import lv.acnbootcamp.fixmycity.dto.LoginResponse;
import lv.acnbootcamp.fixmycity.security.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.BadCredentialsException;

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

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager,
                jwtService);
    }

    @Test
    void register_savesNewUser_whenEmailNotTaken() {
        // given
        RegisterRequest request = new RegisterRequest(
                "new.user@example.com", "password123", "New User");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");

        User savedUser = UserTestDataBuilder.aUser()
                .withId(1L)
                .withEmail(request.email())
                .withPassword("hashed-password")
                .withFullName(request.fullName())
                .withRole(Role.CITIZEN)
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

        // Verify that the user is created with the expected registration data
        // and that the CITIZEN role is assigned by the backend
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.CITIZEN);
        assertThat(userCaptor.getValue().getEmail()).isEqualTo(request.email());
        assertThat(userCaptor.getValue().getFullName()).isEqualTo(request.fullName());
        // Verify that the password encoder was called with the submitted password
        verify(passwordEncoder).encode(request.password());
    }

    @Test
    void register_throwsException_whenEmailAlreadyExists() {
        // given
        RegisterRequest request = new RegisterRequest(
                "existing@example.com", "password123", "Existing User");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("existing@example.com");

        // save() should never be reached if the email check fails fast
        verify(userRepository, never()).save(any());
        //verify that password hashing never happens
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void login_returnsJwtAndUserData_whenCredentialsAreValid() {
        // given
        LoginRequest request = new LoginRequest(
                "citizen@example.com",
                "password123"
        );

        User user = UserTestDataBuilder.aUser()
                .withId(1L)
                .withEmail(request.email())
                .withPassword("hashed-password")
                .withFullName("Test Citizen")
                .withRole(Role.CITIZEN)
                .build();

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(jwtService.generateToken(userDetails))
                .thenReturn("test-jwt-token");

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response.accessToken())
                .isEqualTo("test-jwt-token");

        assertThat(response.tokenType())
                .isEqualTo("Bearer");

        assertThat(response.userId())
                .isEqualTo(1L);

        assertThat(response.email())
                .isEqualTo("citizen@example.com");

        assertThat(response.fullName())
                .isEqualTo("Test Citizen");

        assertThat(response.role())
                .isEqualTo(Role.CITIZEN);

        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void login_throwsException_whenCredentialsAreInvalid() {
        // given
        LoginRequest request = new LoginRequest(
                "citizen@example.com",
                "wrong-password"
        );

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // when / then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        // A JWT must never be generated when authentication fails.
        verify(jwtService, never()).generateToken(any());
    }

}