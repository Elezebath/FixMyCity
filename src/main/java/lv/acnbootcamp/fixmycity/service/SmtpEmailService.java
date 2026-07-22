package lv.acnbootcamp.fixmycity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${email.from}")
    private String senderEmail;

    @Value("${password-reset.frontend-url}")
    private String frontendResetUrl;

    @Value("${password-reset.expiration-minutes:30}")
    private long expirationMinutes;

    @Override
    public void sendPasswordResetEmail(String recipientEmail, String resetToken) {
        String resetUrl = UriComponentsBuilder
                .fromUriString(frontendResetUrl)
                .queryParam("token", resetToken)
                .build()
                .toUriString();

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("FixMyCity password reset");

        message.setText(
                """
                Hello,

                We received a request to reset your FixMyCity password.

                Use the following link to set a new password:

                %s

                This link expires in %d minutes and can be used only once.

                If you did not request a password reset, you can ignore this email.

                FixMyCity
                """.formatted(resetUrl, expirationMinutes)
        );

        mailSender.send(message);
    }
}