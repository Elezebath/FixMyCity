package lv.acnbootcamp.fixmycity.validation;

import lv.acnbootcamp.fixmycity.exception.FileTooLargeException;
import lv.acnbootcamp.fixmycity.exception.InvalidFileTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Component
public class FileValidator {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.error("Invalid file type: {}", contentType);
            throw new InvalidFileTypeException(
                    "Invalid file type: " + contentType +
                            ". Allowed types: " + ALLOWED_CONTENT_TYPES);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.error("File too large: {} bytes", file.getSize());
            throw new FileTooLargeException(
                    "File size exceeds maximum of " + MAX_FILE_SIZE + " bytes");
        }
    }
}