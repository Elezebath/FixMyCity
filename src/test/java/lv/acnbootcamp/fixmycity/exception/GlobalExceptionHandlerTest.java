package lv.acnbootcamp.fixmycity.exception;

import lv.acnbootcamp.fixmycity.exception.category.CategoryAlreadyExistsException;
import lv.acnbootcamp.fixmycity.exception.category.CategoryInUseException;
import lv.acnbootcamp.fixmycity.exception.category.CategoryNotFoundException;
import lv.acnbootcamp.fixmycity.exception.incident.IncidentNotFoundException;
import lv.acnbootcamp.fixmycity.exception.incident.InvalidIncidentException;
import lv.acnbootcamp.fixmycity.exception.incident.InvalidPriorityException;
import lv.acnbootcamp.fixmycity.exception.incident.InvalidStatusException;
import lv.acnbootcamp.fixmycity.exception.user.CompanyNotFoundException;
import lv.acnbootcamp.fixmycity.exception.user.EmailAlreadyExistsException;
import lv.acnbootcamp.fixmycity.exception.user.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.core.MethodParameter;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.HttpInputMessage;
import jakarta.validation.ConstraintViolationException;

import static org.mockito.Mockito.mock;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleEmailAlreadyExistsException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleEmailExists(
                        new EmailAlreadyExistsException("Email already exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody())
                .containsEntry("error", new EmailAlreadyExistsException("Email already exists").getMessage());
    }

    @Test
    void shouldHandleUserNotFoundException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleNotFound(
                        new UserNotFoundException("User not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("error", "User not found");
    }

    @Test
    void shouldHandleIncidentNotFoundException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleIncidentNotFound(
                        new IncidentNotFoundException("Incident not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("error", "Incident not found");
    }

    @Test
    void shouldHandleCategoryNotFoundException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleCategoryNotFound(
                        new CategoryNotFoundException("Category not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("error", "Category not found");
    }

    @Test
    void shouldHandleCompanyNotFoundException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleCompanyNotFound(
                        new CompanyNotFoundException("Company not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("error", "Company not found");
    }

    @Test
    void shouldHandleInvalidIncidentException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidIncident(
                        new InvalidIncidentException("Invalid incident"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid incident");
    }

    @Test
    void shouldHandleInvalidPriorityException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidPriority(
                        new InvalidPriorityException("Invalid priority"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid priority");
    }

    @Test
    void shouldHandleInvalidStatusException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidStatus(
                        new InvalidStatusException("Invalid status"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid status");
    }

    @Test
    void shouldHandleCategoryAlreadyExistsException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleCategoryAlreadyExists(
                        new CategoryAlreadyExistsException("Category already exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody())
                .containsEntry("error", "Category already exists");
    }

    @Test
    void shouldHandleCategoryInUseException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleCategoryInUse(
                        new CategoryInUseException("Category is in use"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody())
                .containsEntry("error", "Category is in use");
    }

    @Test
    void shouldHandleInvalidPasswordResetTokenException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidPasswordResetToken(
                        new InvalidPasswordResetTokenException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid or expired password reset token");
    }

    @Test
    void shouldHandleInvalidFileTypeException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidFileType(
                        new InvalidFileTypeException("Invalid file type"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid file type");
    }

    @Test
    void shouldHandleFileTooLargeException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleFileTooLarge(
                        new FileTooLargeException("File too large"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "File too large");
    }

    @Test
    void shouldHandleFileStorageException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleFileStorageException(
                        new FileStorageException("Storage error"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .containsEntry("error", "Storage error");
    }

    @Test
    void shouldHandleUnauthorizedException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleUnauthorized(
                        new UnauthorizedException("Unauthorized"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .containsEntry("error", "Unauthorized");
    }

    @Test
    void shouldHandleAuthenticationException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleAuthentication(
                        new BadCredentialsException("Wrong credentials"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid email or password");
    }

    @Test
    void shouldHandleIllegalArgumentException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(
                        new IllegalArgumentException("Invalid id"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid id");
    }

    @Test
    void shouldHandleGenericException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleException(
                        new Exception("Something went wrong"));

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(response.getBody())
                .containsEntry("error", "An unexpected error occurred");
    }

    @Test
    void shouldHandleNoResourceFoundException() {

        ResponseEntity<Map<String, String>> response =
                handler.handleNoResourceFound(
                        new NoResourceFoundException(
                                HttpMethod.GET,
                                "/test",
                                "missing-file.jpg"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("error", "Resource not found");
    }

    private void dummyMethod(Long id) {
    }

    @Test
    void shouldHandleMethodArgumentTypeMismatchException() throws NoSuchMethodException {

        MethodParameter parameter = new MethodParameter(
                getClass().getDeclaredMethod("dummyMethod", Long.class),
                0
        );

        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException(
                        "abc",
                        Long.class,
                        "id",
                        parameter,
                        null
                );

        ResponseEntity<Map<String, String>> response =
                handler.handleTypeMismatch(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Invalid value for parameter 'id'");
    }

    @Test
    void shouldHandleHttpMessageNotReadableException() {

        HttpInputMessage inputMessage = mock(HttpInputMessage.class);

        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException(
                        "Malformed JSON",
                        inputMessage
                );

        ResponseEntity<Map<String, String>> response =
                handler.handleUnreadableMessage(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Malformed request body");
    }


    @Test
    void shouldHandleConstraintViolationException() {

        ConstraintViolationException exception =
                new ConstraintViolationException("Validation failed", null);

        ResponseEntity<Map<String, String>> response =
                handler.handleConstraintViolation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Validation failed");
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() throws NoSuchMethodException {

        MethodParameter parameter = new MethodParameter(
                getClass().getDeclaredMethod("dummyMethod", Long.class),
                0
        );

        BindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");

        bindingResult.addError(
                new FieldError(
                        "request",
                        "email",
                        "Email is required"
                )
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        parameter,
                        bindingResult
                );

        ResponseEntity<Map<String, String>> response =
                handler.handleValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .containsEntry("email", "Email is required");
    }

    @Test
    void shouldHandleHandlerMethodValidationException() {

        MethodValidationResult validationResult =
                mock(MethodValidationResult.class);

        HandlerMethodValidationException exception =
                new HandlerMethodValidationException(validationResult);

        ResponseEntity<Map<String, String>> response =
                handler.handleHandlerValidation(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .containsEntry("error", "Invalid request parameter");
    }
}