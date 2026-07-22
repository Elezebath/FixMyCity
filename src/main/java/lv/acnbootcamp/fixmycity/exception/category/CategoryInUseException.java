package lv.acnbootcamp.fixmycity.exception.category;

public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(String message) {
        super(message);
    }
}