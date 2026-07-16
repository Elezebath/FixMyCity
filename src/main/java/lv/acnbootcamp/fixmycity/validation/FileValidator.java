package lv.acnbootcamp.fixmycity.validation;

import lv.acnbootcamp.fixmycity.exception.FileTooLargeException;
import lv.acnbootcamp.fixmycity.exception.InvalidFileTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
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

    //  value comes from Spring config at runtime
    @Value("${spring.servlet.multipart.max-file-size:5MB}")
    private DataSize maxFileSize;

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

        if (file.getSize() > maxFileSize.toBytes()) {
            log.error("File too large: {} bytes", file.getSize());
            throw new FileTooLargeException(
                    "File size exceeds maximum of " + maxFileSize.toMegabytes() + "MB");
        }
    }
}