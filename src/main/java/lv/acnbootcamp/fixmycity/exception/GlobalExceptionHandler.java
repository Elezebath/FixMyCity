package lv.acnbootcamp.fixmycity.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final String ERROR = "error";

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailExists(EmailAlreadyExistsException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body); // 409
    }

    // Triggered by @Valid failures (e.g. blank email, short password).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors); // 400
    }

    /**
     * Returns 401 when login credentials are invalid.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthentication(AuthenticationException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR, "Invalid email or password");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    /**
     * Returns 404 when an incident is not found.
     */
    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleIncidentNotFound(IncidentNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body); // 404
    }

    /**
     * Returns 404 when a category is not found.
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCategoryNotFound(CategoryNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body); // 404
    }

    /**
     * Returns 400 when incident data is invalid.
     */
    @ExceptionHandler(InvalidIncidentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidIncident(InvalidIncidentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body); // 400
    }

    /**
     * Returns 400 when priority is invalid.
     */
    @ExceptionHandler(InvalidPriorityException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPriority(InvalidPriorityException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body); // 400
    }

    /**
     * Returns 400 when an invalid argument is provided (e.g., invalid ID).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body); // 400
    }

    /**
     * Returns 404 when a company is not found.
     */
    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCompanyNotFound(CompanyNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body); // 404
    }

    /**
     * Returns 404 when a user is not found.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UserNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body); // 404
    }

    /**
     * Returns 400 when an invalid incident status is provided.
     */
    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<Map<String, String>> handleInvalidStatus(InvalidStatusException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body); // 400
    }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        Map<String, String> body = new HashMap<>();
        body.put(ERROR, "Invalid value for parameter '" + ex.getName() + "'");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableMessage(
            HttpMessageNotReadableException ex) {

        Map<String, String> body = new HashMap<>();
        body.put(ERROR, "Malformed request body");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {

        log.error("Unexpected error", ex);

        Map<String, String> body = new HashMap<>();
        body.put(ERROR, "An unexpected error occurred");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
