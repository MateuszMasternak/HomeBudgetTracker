package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.handler.exception.FileProcessingException;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.BankName;
import com.rainy.homebudgettracker.transaction.service.extractor.TransactionExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class ImportTransactionServiceImpl implements ImportTransactionService {
    private final TransactionService transactionService;
    private final List<TransactionExtractor> extractors;

    @Override
    public List<TransactionResponse> extractTransactions(MultipartFile file, BankName bankName) {
        TransactionExtractor extractor = extractors.stream()
                .filter(e -> e.supports(bankName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported bank name: " + bankName));

        try {
            return extractor.extract(file.getInputStream());
        } catch (IOException e) {
            log.error("Error while extracting transactions from file: {}", file.getOriginalFilename(), e);
            throw new FileProcessingException("Error while extracting data from file: " + file.getOriginalFilename());
        }
    }

    @Override
    public boolean importTransactions(UUID accountId, List<TransactionRequest> transactions) {
        for (TransactionRequest transaction : transactions) {
            transactionService.createTransactionForCurrentUser(accountId, transaction);
        }

        return true;
    }
}