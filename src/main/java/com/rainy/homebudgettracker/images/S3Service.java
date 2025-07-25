package com.rainy.homebudgettracker.images;

import com.rainy.homebudgettracker.handler.exception.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;
    @Value("${aws.s3.expiration-time}")
    private Long expirationTime;

    public String uploadFile(MultipartFile file, String userSub, UUID transactionId) {
        try {
            File tempFile = convertMultipartFileToCompressedFile(file);
            String key = createKeyForImage(userSub, transactionId);
            s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(), tempFile.toPath());
            tempFile.delete();
            return key;
        } catch (IOException e) {
            throw new ImageUploadException("Failed to upload image", e);
        }
    }

    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
    }

    public String createPresignedGetUrl(String key) {
        try (S3Presigner presigner = S3Presigner.create()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expirationTime))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(getObjectPresignRequest);
            return presignedGetObjectRequest.url().toExternalForm();
        }
    }

    private String createKeyForImage(String userSub, UUID transactionId) {
        String name = userSub + "_" + transactionId;
        return "images/" + UUID.nameUUIDFromBytes(name.getBytes());
    }

    private File convertMultipartFileToCompressedFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("temp", file.getOriginalFilename());
        file.transferTo(tempFile);
        return ImageCompressor.compressImage(tempFile);
    }
}
