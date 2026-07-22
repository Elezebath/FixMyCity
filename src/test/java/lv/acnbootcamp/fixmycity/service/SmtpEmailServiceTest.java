package lv.acnbootcamp.fixmycity.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private SmtpEmailService smtpEmailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(
                smtpEmailService,
                "senderEmail",
                "noreply@fixmycity.com"
        );

        ReflectionTestUtils.setField(
                smtpEmailService,
                "frontendResetUrl",
                "http://localhost:5173/reset-password"
        );

        ReflectionTestUtils.setField(
                smtpEmailService,
                "expirationMinutes",
                30L
        );
    }

    @Test
    void shouldSendPasswordResetEmail() {

        smtpEmailService.sendPasswordResetEmail(
                "user@example.com",
                "abc123"
        );

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals(
                "noreply@fixmycity.com",
                message.getFrom()
        );

        assertArrayEquals(
                new String[]{"user@example.com"},
                message.getTo()
        );

        assertEquals(
                "FixMyCity password reset",
                message.getSubject()
        );

        assertTrue(
                message.getText().contains("abc123")
        );

        assertTrue(
                message.getText().contains("30")
        );

        assertTrue(
                message.getText().contains("http://localhost:5173/reset-password")
        );
    }

    @Test
    void shouldIncludeResetTokenInResetLink() {

        smtpEmailService.sendPasswordResetEmail(
                "user@example.com",
                "abc123"
        );

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertTrue(
                message.getText().contains(
                        "http://localhost:5173/reset-password?token=abc123"
                )
        );
    }



}