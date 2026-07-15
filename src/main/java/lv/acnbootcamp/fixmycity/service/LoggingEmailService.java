package lv.acnbootcamp.fixmycity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LoggingEmailService implements EmailService {
//later replaced with this ->
//    @Service
//    public class SmtpEmailService implements EmailService {
//
//        private final JavaMailSender mailSender;}

    @Override
    public void sendPasswordResetEmail(
            String recipientEmail,
            String resetToken) {
        log.info("PASSWORD RESET — email: {}, token: {}", recipientEmail, resetToken);
    }
}
