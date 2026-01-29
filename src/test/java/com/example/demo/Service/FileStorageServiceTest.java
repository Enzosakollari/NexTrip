package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private String originalUserDir;
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        fileStorageService = new FileStorageService();
    }

    @AfterEach
    void tearDown() {
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void storePackImage_whenValidFile_storesAndReturnsUrl() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "sample".getBytes(StandardCharsets.UTF_8)
        );

        // Act
        String url = fileStorageService.storePackImage(file);

        // Assert
        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/packs/"));
        String filename = url.replace("/uploads/packs/", "");
        Path storedPath = tempDir.resolve("uploads").resolve("packs").resolve(filename);
        assertTrue(Files.exists(storedPath));
        assertArrayEquals("sample".getBytes(StandardCharsets.UTF_8), Files.readAllBytes(storedPath));
    }

    @Test
    void storePackImage_whenFileIsNull_returnsNull() {
        // Arrange
        MultipartFile file = null;

        // Act
        String url = fileStorageService.storePackImage(file);

        // Assert
        assertNull(url);
    }

    @Test
    void storeBusinessImage_whenValidFile_storesAndReturnsUrl() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.jpg",
                "image/jpeg",
                "logo".getBytes(StandardCharsets.UTF_8)
        );

        // Act
        String url = fileStorageService.storeBusinessImage(file);

        // Assert
        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/business/"));
        String filename = url.replace("/uploads/business/", "");
        Path storedPath = tempDir.resolve("uploads").resolve("business").resolve(filename);
        assertTrue(Files.exists(storedPath));
        assertArrayEquals("logo".getBytes(StandardCharsets.UTF_8), Files.readAllBytes(storedPath));
    }

    @Test
    void storeBusinessImage_whenEmptyFile_returnsNull() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        // Act
        String url = fileStorageService.storeBusinessImage(file);

        // Assert
        assertNull(url);
    }

    @Test
    void storePackImages_whenFilesProvided_returnsOnlyStoredUrls() {
        // Arrange
        MockMultipartFile validFile = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                "data".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        // Act
        List<String> urls = fileStorageService.storePackImages(new MultipartFile[] { validFile, emptyFile });

        // Assert
        assertEquals(1, urls.size());
        assertTrue(urls.get(0).startsWith("/uploads/packs/"));
    }

    @Test
    void storePackImages_whenNullArray_returnsEmptyList() {
        // Arrange
        MultipartFile[] files = null;

        // Act
        List<String> urls = fileStorageService.storePackImages(files);

        // Assert
        assertNotNull(urls);
        assertTrue(urls.isEmpty());
    }
}