package com.university.backend.common.file;

import com.university.backend.common.error.ApiException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final long MAX_UPLOAD_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private final FileStorageProperties properties;
    private Path rootPath;
    private List<Path> legacyUploadPaths = List.of();

    public FileStorageService(FileStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        try {
            this.rootPath = Paths.get(properties.rootPath()).toAbsolutePath().normalize();
            Files.createDirectories(rootPath);
            this.legacyUploadPaths = resolveLegacyUploadPaths();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to initialize file storage", exception);
        }
    }

    public StoredFile store(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            throw ApiException.badRequest("Uploaded file must not be empty");
        }
        if (multipartFile.getSize() > MAX_UPLOAD_BYTES) {
            throw ApiException.badRequest("Uploaded image must not exceed 5MB");
        }

        String extension = StringUtils.getFilenameExtension(multipartFile.getOriginalFilename());
        String normalizedExtension = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        String contentType = multipartFile.getContentType();
        if (!ALLOWED_EXTENSIONS.contains(normalizedExtension) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw ApiException.badRequest("Only jpg, png, gif, and webp images are allowed");
        }
        String safeExtension = "." + normalizedExtension;
        LocalDate today = LocalDate.now();
        String relativePath = "%d/%02d/%s%s".formatted(
            today.getYear(),
            today.getMonthValue(),
            UUID.randomUUID().toString().replace("-", ""),
            safeExtension
        );

        try {
            Path destination = rootPath.resolve(relativePath).normalize();
            if (!destination.startsWith(rootPath)) {
                throw ApiException.badRequest("Invalid file path");
            }
            Files.createDirectories(destination.getParent());
            Files.copy(multipartFile.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(
                relativePath.replace("\\", "/"),
                multipartFile.getOriginalFilename(),
                multipartFile.getContentType(),
                multipartFile.getSize(),
                properties.publicBaseUrl() + "/" + relativePath.replace("\\", "/")
            );
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store uploaded file");
        }
    }

    public Resource load(String relativePath) {
        return loadFromRoot(rootPath, relativePath);
    }

    public Resource loadLegacy(String storedPath) {
        String relativePath = normalizeLegacyPath(storedPath);
        for (Path legacyUploadPath : legacyUploadPaths) {
            Resource resource = loadFromRootIfExists(legacyUploadPath, relativePath);
            if (resource != null) {
                return resource;
            }
        }
        throw ApiException.notFound("File not found");
    }

    private Resource loadFromRoot(Path basePath, String relativePath) {
        try {
            Path filePath = basePath.resolve(relativePath).normalize();
            if (!filePath.startsWith(basePath) || !Files.isRegularFile(filePath) || !Files.isReadable(filePath)) {
                throw ApiException.badRequest("Invalid file path");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw ApiException.notFound("File not found");
            }
            return resource;
        } catch (MalformedURLException exception) {
            throw ApiException.notFound("File not found");
        }
    }

    private Resource loadFromRootIfExists(Path basePath, String relativePath) {
        try {
            Path filePath = basePath.resolve(relativePath).normalize();
            if (!filePath.startsWith(basePath) || !Files.isRegularFile(filePath)) {
                return null;
            }
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    private List<Path> resolveLegacyUploadPaths() {
        Path workingDirectory = Paths.get("").toAbsolutePath().normalize();
        LinkedHashSet<Path> candidates = new LinkedHashSet<>();
        candidates.add(workingDirectory.resolve("upload").normalize());
        candidates.add(workingDirectory.resolve("..").resolve("upload").normalize());
        candidates.add(workingDirectory.resolve("src/main/resources/static/upload").normalize());
        candidates.add(workingDirectory.resolve("..").resolve("src/main/resources/static/upload").normalize());
        candidates.add(workingDirectory.resolve("target/classes/static/upload").normalize());
        candidates.add(workingDirectory.resolve("..").resolve("target/classes/static/upload").normalize());

        List<Path> existingPaths = new ArrayList<>();
        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                existingPaths.add(candidate);
            }
        }
        return existingPaths.isEmpty() ? List.of(workingDirectory.resolve("upload").normalize()) : existingPaths;
    }

    private String normalizeLegacyPath(String storedPath) {
        String cleanedPath = storedPath == null ? "" : storedPath.trim().replace("\\", "/");
        while (cleanedPath.startsWith("/")) {
            cleanedPath = cleanedPath.substring(1);
        }
        if (cleanedPath.startsWith("file/")) {
            cleanedPath = cleanedPath.substring("file/".length());
        }
        if (cleanedPath.startsWith("upload/")) {
            cleanedPath = cleanedPath.substring("upload/".length());
        }
        if (!StringUtils.hasText(cleanedPath) || cleanedPath.contains("..")) {
            throw ApiException.badRequest("Invalid file path");
        }
        return cleanedPath;
    }

    public record StoredFile(
        String relativePath,
        String originalFileName,
        String contentType,
        long size,
        String url
    ) {
    }
}
