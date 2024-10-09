package com.rainy.homebudgettracker.images;

import com.rainy.homebudgettracker.handler.exception.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, Long userId, Long transactionId) throws ImageUploadException {
        try {
            File tempFile = convertMultipartFileToFile(file);
            String key = createKeyForImage(userId, transactionId, tempFile.getName());
            s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(), tempFile.toPath());
            return key;
        } catch (IOException e) {
            throw new ImageUploadException("Failed to upload image");
        }
    }

    public void deleteFile(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
    }

    private String createKeyForImage(Long userId, Long transactionId, String fileName) {
        System.out.println(fileName);
        String extension = fileName.substring(fileName.lastIndexOf("."));
        return "images/" + userId + "_" + transactionId + "_" + LocalDate.now() + extension;
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("temp", file.getOriginalFilename());
        file.transferTo(tempFile);
        return tempFile;
    }
}
