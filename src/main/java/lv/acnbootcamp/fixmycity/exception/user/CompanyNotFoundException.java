package lv.acnbootcamp.fixmycity.exception.user;

public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(String message) {
        super(message);
    }
}
