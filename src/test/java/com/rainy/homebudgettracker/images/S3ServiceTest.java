package com.rainy.homebudgettracker.images;

import com.rainy.homebudgettracker.handler.exception.ImageUploadException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class S3ServiceTest {
    @InjectMocks
    S3Service s3Service;
    @Mock
    S3Client s3Client;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "expirationTime", 3600L);

        when(s3Client.putObject(any(PutObjectRequest.class), any(Path.class))).thenReturn(null);
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(null);
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnKey() throws ImageUploadException {
        try (MockedStatic<ImageCompressor> mockedStatic = mockStatic(ImageCompressor.class)) {
            mockedStatic.when(() -> ImageCompressor.compressImage(any())).thenReturn(new File("test.jpg"));
            MultipartFile file = new MockMultipartFile("test.png", "test.png", "image/png", new byte[100]);
            String userSub = "b848bced-0daf-4ad7-b9c6-4c477ab5a903";
            UUID transactionId = UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903");
            String key = s3Service.uploadFile(file, userSub, transactionId);
            String expectedKey = "images/" + UUID.nameUUIDFromBytes("b848bced-0daf-4ad7-b9c6-4c477ab5a903_b848bced-0daf-4ad7-b9c6-4c477ab5a903".getBytes());
            assertEquals(expectedKey, key);
        }
    }

    @Test
    void shouldDeleteImage() {
        String key = "images/" + UUID.nameUUIDFromBytes("b848bced-0daf-4ad7-b9c6-4c477ab5a903_b848bced-0daf-4ad7-b9c6-4c477ab5a903".getBytes());
        s3Service.deleteFile(key);
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
    }
}