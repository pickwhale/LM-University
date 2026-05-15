package com.university.backend.common.file;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.file-storage")
public record FileStorageProperties(String rootPath, String publicBaseUrl) {
}
