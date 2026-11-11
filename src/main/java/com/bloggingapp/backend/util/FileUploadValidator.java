package com.bloggingapp.backend.util;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

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

        return UUID.randomUUID() + "." + extension;
    }
}
