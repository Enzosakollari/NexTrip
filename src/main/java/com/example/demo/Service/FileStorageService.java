package com.example.demo.Service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path packUploadDir;
    private final Path businessUploadDir;

    public FileStorageService() {
        this.packUploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "packs");
        this.businessUploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "business");
    }

    public String storePackImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeExt = extension == null ? "" : extension.replaceAll("[^A-Za-z0-9]", "");
        String filename = UUID.randomUUID().toString();
        if (!safeExt.isBlank()) {
            filename += "." + safeExt;
        }

        try {
            Files.createDirectories(packUploadDir);
            Path target = packUploadDir.resolve(filename);
            file.transferTo(target);
            return "/uploads/packs/" + filename;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store pack image", ex);
        }
    }

    public String storeBusinessImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeExt = extension == null ? "" : extension.replaceAll("[^A-Za-z0-9]", "");
        String filename = UUID.randomUUID().toString();
        if (!safeExt.isBlank()) {
            filename += "." + safeExt;
        }

        try {
            Files.createDirectories(businessUploadDir);
            Path target = businessUploadDir.resolve(filename);
            file.transferTo(target);
            return "/uploads/business/" + filename;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store business image", ex);
        }
    }

    public List<String> storePackImages(MultipartFile[] files) {
        List<String> stored = new ArrayList<>();
        if (files == null || files.length == 0) {
            return stored;
        }
        for (MultipartFile file : files) {
            String url = storePackImage(file);
            if (url != null && !url.isBlank()) {
                stored.add(url);
            }
        }
        return stored;
    }
}
