package lv.acnbootcamp.fixmycity.service.impl;

import lombok.RequiredArgsConstructor;
import lv.acnbootcamp.fixmycity.entity.user.PasswordResetToken;
import lv.acnbootcamp.fixmycity.entity.user.User;
import lv.acnbootcamp.fixmycity.exception.InvalidPasswordResetTokenException;
import lv.acnbootcamp.fixmycity.repository.PasswordResetTokenRepository;
import lv.acnbootcamp.fixmycity.repository.UserRepository;
import lv.acnbootcamp.fixmycity.service.EmailService;
import lv.acnbootcamp.fixmycity.service.PasswordRecoveryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryServiceImpl
        implements PasswordRecoveryService {

    private static final int TOKEN_SIZE_BYTES = 32;

    private final UserRepository userRepository;

    private final PasswordResetTokenRepository
            passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Value("${password-reset.expiration-minutes:30}")
    private long expirationMinutes;

    /**
     * Creates a password-reset token if the email belongs
     * to an existing user.
     *
     * If the email does not exist, this method does nothing.
     * The controller still returns the same generic response
     * so that registered email addresses are not exposed.
     */
    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {

                    /*
                     * A new request invalidates previous
                     * password-reset tokens for this user.
                     */
                    passwordResetTokenRepository.deleteAllByUser(user);

                    String rawToken = generateSecureToken();

                    String tokenHash = hashToken(rawToken);

                    PasswordResetToken resetToken =
                            PasswordResetToken
                                    .builder()
                                    .tokenHash(tokenHash)
                                    .user(user)
                                    .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                                    .used(false)
                                    .build();

                    passwordResetTokenRepository
                            .save(resetToken);

                    /*
                     * LoggingEmailService logs the raw token
                     * during local development.
                     *
                     * Later, an SMTP implementation can send
                     * the token in an email without changing
                     * this service.
                     */
                    emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
                });
    }

    /**
     * Validates a reset token and stores the new password
     * as a BCrypt hash.
     */
    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash).orElseThrow(
                InvalidPasswordResetTokenException::new);

        if (resetToken.isUsed()) {throw new InvalidPasswordResetTokenException();}

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidPasswordResetTokenException();
        }

        User user = resetToken.getUser();

        /*
         * User.java has a "password" field, so this setter
         * is correct.
         */
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        /*
         * A successful token can be used only once.
         */
        resetToken.setUsed(true);

        passwordResetTokenRepository.save(resetToken);
    }

    /**
     * Generates 32 cryptographically secure random bytes
     * and converts them to a URL-safe string.
     */
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_SIZE_BYTES];

        secureRandom.nextBytes(tokenBytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    /**
     * Hashes the raw reset token using SHA-256.
     *
     * The raw token is delivered to the user, while only
     * the hash is stored in the database.
     */
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}