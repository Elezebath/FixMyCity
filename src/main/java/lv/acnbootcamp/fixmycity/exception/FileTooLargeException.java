package lv.acnbootcamp.fixmycity.exception;

/**
 * Exception thrown when a file exceeds the maximum allowed size.
 */
public class FileTooLargeException extends FileStorageException {

    public FileTooLargeException(String message) {
        super(message);
    }

    public FileTooLargeException(String message, Throwable cause) {
        super(message, cause);
    }
}