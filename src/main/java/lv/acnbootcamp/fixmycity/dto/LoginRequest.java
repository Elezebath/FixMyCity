package lv.acnbootcamp.fixmycity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
/**
 * Request payload containing the credentials required to authenticate a user.
 */
public record LoginRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        String password

) {
}