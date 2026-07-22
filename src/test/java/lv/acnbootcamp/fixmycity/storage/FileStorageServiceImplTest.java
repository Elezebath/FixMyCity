package lv.acnbootcamp.fixmycity.storage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lv.acnbootcamp.fixmycity.validation.FileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;


import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceImplTest {

    @Mock
    private FileValidator fileValidator;

    private FileStorageServiceImpl fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageServiceImpl(
                tempDir.toString(),
                fileValidator
        );
    }

    @Test
    void shouldReturnNullWhenFileIsNull() {

        String storedFileName = fileStorageService.storeFile(null);

        assertThat(storedFileName).isNull();

        verifyNoInteractions(fileValidator);
    }

    @Test
    void shouldReturnNullWhenFileIsEmpty() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[0]
        );

        String storedFileName = fileStorageService.storeFile(file);

        assertThat(storedFileName).isNull();
        verifyNoInteractions(fileValidator);
    }

    @Test
    void shouldStoreFileSuccessfully() throws IOException {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                "Hello World".getBytes()
        );

        String storedFileName = fileStorageService.storeFile(file);

        assertThat(storedFileName).isNotNull();
        assertThat(storedFileName).endsWith(".jpg");

        Path storedFile = tempDir.resolve(storedFileName);

        assertThat(Files.exists(storedFile)).isTrue();
        assertThat(Files.readString(storedFile)).isEqualTo("Hello World");

        verify(fileValidator).validate(file);
    }

    @Test
    void shouldLoadFileSuccessfully() {

        Path filePath = fileStorageService.loadFile("photo.jpg");

        assertThat(filePath).isEqualTo(tempDir.resolve("photo.jpg"));
    }

    @Test
    void shouldReturnNullWhenLoadingBlankFileName() {

        Path filePath = fileStorageService.loadFile("");

        assertThat(filePath).isNull();
    }

    @Test
    void shouldDoNothingWhenDeletingBlankFileName() {

        assertThatCode(() -> fileStorageService.deleteFile(""))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldDeleteExistingFile() throws IOException {

        Path file = tempDir.resolve("photo.jpg");
        Files.writeString(file, "Hello World");

        assertThat(Files.exists(file)).isTrue();

        fileStorageService.deleteFile("photo.jpg");

        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void shouldDelegateFileValidation() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                "Hello World".getBytes()
        );

        fileStorageService.validateFile(file);

        verify(fileValidator).validate(file);
    }

    @Test
    void shouldReturnContentTypeWhenFileIsValid() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                "Hello World".getBytes()
        );

        String contentType = fileStorageService.getContentType(file);

        assertThat(contentType).isEqualTo("image/jpeg");
    }

    @Test
    void shouldReturnNullContentTypeWhenFileIsNull() {
        assertThat(fileStorageService.getContentType(null)).isNull();
    }

    @Test
    void shouldReturnNullContentTypeWhenFileIsEmpty() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThat(fileStorageService.getContentType(file)).isNull();
    }


}