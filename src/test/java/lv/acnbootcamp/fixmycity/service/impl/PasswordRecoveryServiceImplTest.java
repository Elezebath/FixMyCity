package lv.acnbootcamp.fixmycity.service.impl;

import lv.acnbootcamp.fixmycity.entity.user.PasswordResetToken;
import lv.acnbootcamp.fixmycity.entity.user.User;
import lv.acnbootcamp.fixmycity.exception.InvalidPasswordResetTokenException;
import lv.acnbootcamp.fixmycity.repository.PasswordResetTokenRepository;
import lv.acnbootcamp.fixmycity.repository.UserRepository;
import lv.acnbootcamp.fixmycity.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordRecoveryServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordRecoveryServiceImpl passwordRecoveryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateResetTokenWhenUserExists() {

        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        passwordRecoveryService.requestPasswordReset("test@example.com");

        verify(userRepository)
                .findByEmail("test@example.com");

        verify(passwordResetTokenRepository)
                .deleteAllByUser(user);

        verify(passwordResetTokenRepository)
                .save(any(PasswordResetToken.class));

        verify(emailService)
                .sendPasswordResetEmail(
                        eq("test@example.com"),
                        any(String.class)
                );
    }

    @Test
    void shouldDoNothingWhenUserDoesNotExist() {

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        passwordRecoveryService.requestPasswordReset("test@example.com");

        verify(userRepository)
                .findByEmail("test@example.com");

        verifyNoInteractions(passwordResetTokenRepository);

        verifyNoInteractions(emailService);
    }

    @Test
    void shouldResetPasswordSuccessfully() {

        User user = new User();
        user.setPassword("oldPassword");

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(passwordResetTokenRepository.findByTokenHash(any(String.class)))
                .thenReturn(Optional.of(resetToken));

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedPassword");

        passwordRecoveryService.resetPassword(
                "rawToken",
                "newPassword"
        );

        assertEquals(
                "encodedPassword",
                user.getPassword()
        );

        assertTrue(resetToken.isUsed());

        verify(passwordEncoder)
                .encode("newPassword");

        verify(userRepository)
                .save(user);

        verify(passwordResetTokenRepository)
                .save(resetToken);
    }

    @Test
    void shouldThrowWhenTokenDoesNotExist() {

        when(passwordResetTokenRepository.findByTokenHash(any(String.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> passwordRecoveryService.resetPassword(
                        "rawToken",
                        "newPassword"
                )
        );

        verifyNoInteractions(passwordEncoder);

        verify(userRepository, never()).save(any(User.class));

        verify(passwordResetTokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void shouldThrowWhenTokenAlreadyUsed() {

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .used(true)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        when(passwordResetTokenRepository.findByTokenHash(any(String.class)))
                .thenReturn(Optional.of(resetToken));

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> passwordRecoveryService.resetPassword(
                        "rawToken",
                        "newPassword"
                )
        );

        verifyNoInteractions(passwordEncoder);

        verify(userRepository, never()).save(any(User.class));

        verify(passwordResetTokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void shouldThrowWhenTokenExpired() {

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .used(false)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(passwordResetTokenRepository.findByTokenHash(any(String.class)))
                .thenReturn(Optional.of(resetToken));

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> passwordRecoveryService.resetPassword(
                        "rawToken",
                        "newPassword"
                )
        );

        verifyNoInteractions(passwordEncoder);

        verify(userRepository, never()).save(any(User.class));

        verify(passwordResetTokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

}