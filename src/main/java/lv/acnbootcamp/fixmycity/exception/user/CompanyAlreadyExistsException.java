package lv.acnbootcamp.fixmycity.exception.user;

public class CompanyAlreadyExistsException extends RuntimeException {

    public CompanyAlreadyExistsException(String message) {
        super(message);
    }
}