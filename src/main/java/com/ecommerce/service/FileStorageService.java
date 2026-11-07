package com.ecommerce.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.exception.BadRequestException;

@Service
public class FileStorageService {

    public static final String AVATARS = "avatars";
    public static final String PRODUCTS = "products";

    private static final Path BASE_DIR = Paths.get("uploads");
    private static final Set<String> ALLOWED_DIRS = Set.of(AVATARS, PRODUCTS);

    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "webp");

    public String save(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BadRequestException("File is too large (max 5 MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only JPEG, PNG or WEBP images are allowed");
        }

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BadRequestException("Invalid file extension");
        }

        try {
            Path dir = resolveDir(subDir);
            Files.createDirectories(dir);
            String fileName = UUID.randomUUID() + "." + ext.toLowerCase();
            Files.copy(file.getInputStream(), dir.resolve(fileName));
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not save file: " + e.getMessage(), e);
        }
    }

    public void delete(String fileName, String subDir) {
        if (fileName == null) return;
        try {
            Files.deleteIfExists(resolveDir(subDir).resolve(fileName));
        } catch (IOException e) {
            // log warning nhưng không throw, không chặn flow chính
        }
    }

    // Chỉ cho phép thư mục con nằm trong danh sách, chặn kiểu "../.."
    private Path resolveDir(String subDir) {
        if (!ALLOWED_DIRS.contains(subDir)) {
            throw new IllegalArgumentException("Invalid upload directory: " + subDir);
        }
        return BASE_DIR.resolve(subDir);
    }
}