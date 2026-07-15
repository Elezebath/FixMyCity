package lv.acnbootcamp.fixmycity.controller;

import jakarta.validation.Valid;
import lv.acnbootcamp.fixmycity.dto.*;
import lv.acnbootcamp.fixmycity.service.AuthService;
import lv.acnbootcamp.fixmycity.service.PasswordRecoveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordRecoveryService passwordRecoveryService;

    public AuthController(AuthService authService, PasswordRecoveryService passwordRecoveryService) {
        this.authService = authService;
        this.passwordRecoveryService = passwordRecoveryService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and returns a JWT access token.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Starts the password-recovery process.
     *
     * The same response is returned whether the email
     * exists or not, so registered email addresses
     * are not exposed.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse>
    forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordRecoveryService.requestPasswordReset(request.email());

        return ResponseEntity.ok(new MessageResponse(
                        "If an account with that email exists, "
                                + "password reset instructions "
                                + "have been sent.")
        );
    }

    /**
     * Sets a new password using a valid,
     * unexpired, unused reset token.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse>
    resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordRecoveryService.resetPassword(request.token(), request.newPassword());

        return ResponseEntity.ok(
                new MessageResponse("Password has been reset successfully."));
    }
}