package lv.acnbootcamp.fixmycity.service;

public interface PasswordRecoveryService {

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);
}