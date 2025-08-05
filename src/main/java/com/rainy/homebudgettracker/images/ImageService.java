package com.rainy.homebudgettracker.images;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.transaction.Transaction;
import com.rainy.homebudgettracker.transaction.repository.TransactionRepository;
import com.rainy.homebudgettracker.transaction.service.TransactionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final S3Service s3Service;
    private final CloudfrontService cloudfrontService;
    private final TransactionRepository transactionRepository;
    @Value("${aws.transaction-response-url-type}")
    private String transactionResponseUrlType;

    public String getImageUrl(Transaction transaction) {
        return getUrl(transaction);
    }

    public String getImageUrl(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RecordDoesNotExistException("Transaction with id " + id + " does not exist."));
        return getUrl(transaction);
    }

    private String getUrl(Transaction transaction) {
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
