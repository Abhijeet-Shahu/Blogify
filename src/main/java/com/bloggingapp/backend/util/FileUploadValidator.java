package com.bloggingapp.backend.util;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public final class FileUploadValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private FileUploadValidator() {
    }

    public static String createSafeImageFilename(MultipartFile file, long maxBytes) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded image is empty");
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("Uploaded image exceeds the maximum allowed size");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()
                || !originalFilename.equals(java.nio.file.Path.of(originalFilename).getFileName().toString())) {
            throw new IllegalArgumentException("Invalid image filename");
        }

        int extensionSeparator = originalFilename.lastIndexOf('.');
        if (extensionSeparator <= 0 || extensionSeparator == originalFilename.length() - 1) {
            throw new IllegalArgumentException("Image filename must include a valid extension");
        }

        String extension = originalFilename.substring(extensionSeparator + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)
                || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported image type");
        }
        validateMagicBytes(file, extension);

        return UUID.randomUUID() + "." + extension;
    }

    private static void validateMagicBytes(MultipartFile file, String extension) {
        try {
            byte[] bytes = file.getBytes();
            boolean valid = switch (extension) {
                case "jpg", "jpeg" -> bytes.length >= 3
                        && (bytes[0] & 0xFF) == 0xFF
                        && (bytes[1] & 0xFF) == 0xD8
                        && (bytes[2] & 0xFF) == 0xFF;
                case "png" -> hasPrefix(bytes, new byte[] {
                        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
                case "gif" -> startsWithAscii(bytes, "GIF87a") || startsWithAscii(bytes, "GIF89a");
                case "webp" -> startsWithAscii(bytes, "RIFF")
                        && bytes.length >= 12
                        && startsWithAscii(bytes, "WEBP", 8);
                default -> false;
            };
            if (!valid) {
                throw new IllegalArgumentException("Image content does not match its declared type");
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to inspect uploaded image", exception);
        }
    }

    private static boolean hasPrefix(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (value[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean startsWithAscii(byte[] value, String prefix) {
        return startsWithAscii(value, prefix, 0);
    }

    private static boolean startsWithAscii(byte[] value, String prefix, int offset) {
        byte[] expected = prefix.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        if (value.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (value[offset + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }
}
