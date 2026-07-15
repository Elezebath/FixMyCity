package lv.acnbootcamp.fixmycity.exception.incident;

public class InvalidStatusException extends RuntimeException {
    public InvalidStatusException(String message) {
        super(message);
    }
}
