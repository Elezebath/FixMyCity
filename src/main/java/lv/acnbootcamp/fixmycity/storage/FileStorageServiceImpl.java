package lv.acnbootcamp.fixmycity.storage;

import lombok.extern.slf4j.Slf4j;
import lv.acnbootcamp.fixmycity.exception.FileStorageException;
import lv.acnbootcamp.fixmycity.validation.FileValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementation of FileStorageService that stores files on the local filesystem.
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {
    private final Path fileStorageLocation;
    private final FileValidator fileValidator;

    public FileStorageServiceImpl(@Value("${APP_UPLOAD_PATH:uploads}") String uploadPath, FileValidator fileValidator) {
        this.fileStorageLocation = Paths.get(uploadPath).toAbsolutePath().normalize();
        this.fileValidator = fileValidator;
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Created upload directory at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new FileStorageException(
                    "Could not create upload directory: " + this.fileStorageLocation, ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        fileValidator.validate(file);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + fileExtension;

        try {
            if (originalFileName.contains("..")) {
                log.error("Invalid file path: {}", originalFileName);
                throw new FileStorageException(
                        "Invalid file path: " + originalFileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation,
                    StandardCopyOption.REPLACE_EXISTING);

            log.info("Stored file: {} at {}", originalFileName, targetLocation);
            return fileName;

        } catch (IOException ex) {
            log.error("Could not store file: {}", originalFileName, ex);
            throw new FileStorageException(
                    "Could not store file: " + originalFileName, ex);
        }
    }

    @Override
    public Path loadFile(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return filePath;
        } catch (Exception ex) {
            log.error("Could not load file: {}", fileName, ex);
            throw new FileStorageException("Could not load file: " + fileName, ex);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return;
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", fileName);
        } catch (IOException ex) {
            log.error("Could not delete file: {}", fileName, ex);
            throw new FileStorageException("Could not delete file: " + fileName, ex);
        }
    }

    @Override
    public void validateFile(MultipartFile file) {
        fileValidator.validate(file);
    }

    @Override
    public String getContentType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        return file.getContentType();
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(dotIndex).toLowerCase();
        }
        return "";
    }
}