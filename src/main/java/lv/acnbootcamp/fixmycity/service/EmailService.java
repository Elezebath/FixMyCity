package lv.acnbootcamp.fixmycity.service;

public interface EmailService {

    void sendPasswordResetEmail(String recipientEmail, String resetToken);
}
