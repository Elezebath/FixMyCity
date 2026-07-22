package lv.acnbootcamp.fixmycity.storage;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * Service interface for handling file storage operations.
 */
public interface FileStorageService {

    /**
     * Store a file and return the path where it was stored.
     *
     * @param file the file to store
     * @return the relative path where the file was stored
     */
    String storeFile(MultipartFile file);

    /**
     * Load a file from storage.
     *
     * @param fileName the name of the file to load
     * @return the path to the file
     */
    Path loadFile(String fileName);

    /**
     * Delete a file from storage.
     *
     * @param fileName the name of the file to delete
     */
    void deleteFile(String fileName);


    /**
     * Validate a file for upload.
     *
     * @param file the file to validate
     */
    void validateFile(MultipartFile file);

    /**
     * Get the content type of a file.
     *
     * @param file the file
     * @return the content type
     */
    String getContentType(MultipartFile file);
}