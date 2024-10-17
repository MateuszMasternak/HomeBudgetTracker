package com.rainy.homebudgettracker.images;

import com.rainy.homebudgettracker.transaction.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ImageServiceTest {
    @InjectMocks
    ImageService imageService;
    @Mock
    S3Service s3Service;
    @Mock
    CloudfrontService cloudfrontService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(s3Service.createPresignedGetUrl("test_s3.jpg")).thenReturn("https://test.s3.amazonaws.com/test_s3.jpg");
        when(cloudfrontService.createGetUrl("test_cloudfront.jpg")).thenReturn("https://test.cloudfront.net/test_cloudfront.jpg");
        when(cloudfrontService.createSignedGetURL("test_cloudfront_signed.jpg")).thenReturn("https://test.cloudfront.net/test_cloudfront_signed.jpg");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void shouldReturnS3ImageUrl() {
        Transaction transaction = Transaction.builder()
                .imageFilePath("test_s3.jpg")
                .build();

        ReflectionTestUtils.setField(imageService, "transactionResponseUrlType", "s3");
        String imageUrl = imageService.getImageUrl(transaction);

        assertEquals("https://test.s3.amazonaws.com/test_s3.jpg", imageUrl);
    }

    @Test
    public void shouldReturnCloudfrontImageUrl() {
        Transaction transaction = Transaction.builder()
                .imageFilePath("test_cloudfront.jpg")
                .build();

        ReflectionTestUtils.setField(imageService, "transactionResponseUrlType", "cloudfront");
        String imageUrl = imageService.getImageUrl(transaction);

        assertEquals("https://test.cloudfront.net/test_cloudfront.jpg", imageUrl);
    }

    @Test
    public void shouldReturnCloudfrontSignedImageUrl() {
        Transaction transaction = Transaction.builder()
                .imageFilePath("test_cloudfront_signed.jpg")
                .build();

        ReflectionTestUtils.setField(imageService, "transactionResponseUrlType", "cloudfront-signed");
        String imageUrl = imageService.getImageUrl(transaction);

        assertEquals("https://test.cloudfront.net/test_cloudfront_signed.jpg", imageUrl);
    }
}