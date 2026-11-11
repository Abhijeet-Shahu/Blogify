package com.bloggingapp.backend.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class FileUploadValidatorTest {

    @Test
    void acceptsSupportedImageWithSafeGeneratedName() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "cover.png", "image/png", new byte[] {1, 2, 3});

        String generatedName = FileUploadValidator.createSafeImageFilename(file, 1024);

        assertDoesNotThrow(() -> java.util.UUID.fromString(
                generatedName.substring(0, generatedName.length() - 4)));
    }

    @Test
    void rejectsPathTraversalFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "../cover.png", "image/png", new byte[] {1});

        assertThrows(IllegalArgumentException.class,
                () -> FileUploadValidator.createSafeImageFilename(file, 1024));
    }

    @Test
    void rejectsUnsupportedExtensionAndContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "script.html", "text/html", new byte[] {1});

        assertThrows(IllegalArgumentException.class,
                () -> FileUploadValidator.createSafeImageFilename(file, 1024));
    }

    @Test
    void rejectsOversizedImage() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "cover.jpg", "image/jpeg", new byte[] {1, 2, 3});

        assertThrows(IllegalArgumentException.class,
                () -> FileUploadValidator.createSafeImageFilename(file, 2));
    }
}
