package lv.acnbootcamp.fixmycity.exception;

/**
 * Exception thrown when a file type is not allowed.
 */
public class InvalidFileTypeException extends FileStorageException {

    public InvalidFileTypeException(String message) {
        super(message);
    }

    public InvalidFileTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}