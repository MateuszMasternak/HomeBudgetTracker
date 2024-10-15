package com.rainy.homebudgettracker.images;

import com.rainy.homebudgettracker.transaction.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final S3Service s3Service;
    private final CloudfrontService cloudfrontService;
    @Value("${aws.transaction-response-url-type}")
    private String transactionResponseUrlType;

    public String getImageUrl(Transaction transaction) {
        if (transaction.getImageFilePath() == null || transaction.getImageFilePath().isEmpty()) {
            return null;
        }

        return switch (transactionResponseUrlType) {
            case "cloudfront-signed" -> cloudfrontService.createSignedGetURL(transaction.getImageFilePath());
            case "cloudfront" -> cloudfrontService.createGetUrl(transaction.getImageFilePath());
            case "s3" -> s3Service.createPresignedGetUrl(transaction.getImageFilePath());
            default -> null;
        };
    }
}
