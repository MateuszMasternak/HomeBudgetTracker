package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.BankName;
import com.rainy.homebudgettracker.transaction.service.extractor.TransactionExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImportTransactionServiceImpl implements ImportTransactionService {
    private final TransactionService transactionService;
    private final List<TransactionExtractor> extractors;

    @Override
    public List<TransactionResponse> extractTransactions(MultipartFile file, BankName bankName) throws IOException {
        TransactionExtractor extractor = extractors.stream()
                .filter(e -> e.supports(bankName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported bank name: " + bankName));

        return extractor.extract(file.getInputStream());
    }

    @Override
    public boolean importTransactions(UUID accountId, List<TransactionRequest> transactions)
            throws RecordDoesNotExistException, UserIsNotOwnerException {
        for (TransactionRequest transaction : transactions) {
            transactionService.createTransactionForCurrentUser(accountId, transaction);
        }

        return true;
    }
}