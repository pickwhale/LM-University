package com.university.backend.file.controller;

import com.university.backend.common.api.ApiResponse;
import com.university.backend.common.file.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileStorageService.StoredFile> upload(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(fileStorageService.store(file));
    }

    @GetMapping("/{year:\\d{4}}/{month:\\d{1,2}}/{fileName:.+}")
    public ResponseEntity<Resource> download(
        @PathVariable String year,
        @PathVariable String month,
        @PathVariable String fileName
    ) {
        String relativePath = year + "/" + month + "/" + fileName;
        Resource resource = fileStorageService.load(relativePath);
        return fileResponse(resource);
    }

    @GetMapping("/legacy/**")
    public ResponseEntity<Resource> downloadLegacy(HttpServletRequest request) {
        String prefix = request.getContextPath() + "/api/v1/files/legacy/";
        String requestUri = request.getRequestURI();
        int startIndex = requestUri.indexOf(prefix);
        if (startIndex < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid legacy file path");
        }
        String storedPath = UriUtils.decode(requestUri.substring(startIndex + prefix.length()), StandardCharsets.UTF_8);
        return fileResponse(fileStorageService.loadLegacy(storedPath));
    }

    private ResponseEntity<Resource> fileResponse(Resource resource) {
        return ResponseEntity
            .ok()
            .header("X-Content-Type-Options", "nosniff")
            .contentType(resolveMediaType(resource))
            .body(resource);
    }

    private MediaType resolveMediaType(Resource resource) {
        try {
            String contentType = Files.probeContentType(resource.getFile().toPath());
            return contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
        } catch (IOException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
