package lv.acnbootcamp.fixmycity.exception.incident;

public class IncidentNotFoundException extends RuntimeException {
    public IncidentNotFoundException(String message) {
        super(message);
    }
}
