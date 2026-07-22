package lv.acnbootcamp.fixmycity.exception;

/**
 * Exception thrown when a file storage operation fails.
 */
public class FileStorageException extends RuntimeException {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}