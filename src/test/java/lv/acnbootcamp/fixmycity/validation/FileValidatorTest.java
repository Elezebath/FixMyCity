package lv.acnbootcamp.fixmycity.validation;

import lv.acnbootcamp.fixmycity.exception.FileTooLargeException;
import lv.acnbootcamp.fixmycity.exception.InvalidFileTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileValidatorTest {

    private FileValidator fileValidator;

    @BeforeEach
    void setUp() {
        fileValidator = new FileValidator();

        ReflectionTestUtils.setField(
                fileValidator,
                "maxFileSize",
                DataSize.ofMegabytes(5)
        );
    }

    @Test
    void shouldNotThrowWhenFileIsNull() {

        assertThatCode(() -> fileValidator.validate(null))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowWhenFileIsEmpty() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThatCode(() -> fileValidator.validate(file))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldValidateValidFile() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                "Hello World".getBytes()
        );

        assertThatCode(() -> fileValidator.validate(file))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionWhenContentTypeIsInvalid() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/x-msdownload",
                "virus".getBytes()
        );

        assertThatThrownBy(() -> fileValidator.validate(file))
                .isInstanceOf(InvalidFileTypeException.class)
                .hasMessageContaining("Invalid file type");
    }

    @Test
    void shouldThrowExceptionWhenContentTypeIsNull() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                null,
                "Hello World".getBytes()
        );

        assertThatThrownBy(() -> fileValidator.validate(file))
                .isInstanceOf(InvalidFileTypeException.class)
                .hasMessageContaining("Invalid file type");
    }

    @Test
    void shouldThrowExceptionWhenFileIsTooLarge() {

        byte[] content = new byte[(int) DataSize.ofMegabytes(5).toBytes() + 1];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                content
        );

        assertThatThrownBy(() -> fileValidator.validate(file))
                .isInstanceOf(FileTooLargeException.class)
                .hasMessageContaining("File size exceeds maximum");
    }
}