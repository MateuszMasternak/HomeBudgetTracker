package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.TransactionUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            UUID accountId, Pageable pageable);

    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            UUID accountId, CategoryRequest categoryName, Pageable pageable);

    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            UUID accountId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);

    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            UUID accountId,
            CategoryRequest categoryName,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);

    TransactionResponse createTransactionForCurrentUser(UUID accountId, TransactionRequest transactionRequest);

    TransactionResponse createTransactionForCurrentUser(
            UUID accountId,
            BigDecimal exchangeRate,
            TransactionRequest transactionRequest);

    void deleteCurrentUserTransaction(UUID transactionId);

    List<TransactionResponse> findCurrentUserTransactionsAsResponses();

    byte[] generateCSVWithCurrentUserTransactions();

    TransactionResponse addImageToCurrentUserTransaction(UUID id, MultipartFile file);

    TransactionResponse deleteImageFromCurrentUserTransaction(UUID id);

    TransactionResponse updateTransactionForCurrentUser(UUID transactionId, TransactionUpdateRequest request);
}
