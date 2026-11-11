package com.bloggingapp.backend.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class FileUploadValidatorMagicBytesTest {

    @Test
    void rejectsPngExtensionWithJpegContent() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "cover.png", "image/png",
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});

        assertThrows(IllegalArgumentException.class,
                () -> FileUploadValidator.createSafeImageFilename(file, 1024));
    }

    @Test
    void rejectsInvalidImageBytes() {
        MockMultipartFile file = new MockMultipartFile(
                "image", "cover.jpg", "image/jpeg", "not-an-image".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> FileUploadValidator.createSafeImageFilename(file, 1024));
    }
}
