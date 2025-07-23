package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.transaction.service.queryfilter.TransactionFilter;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.TransactionUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionService {
    Page<TransactionResponse> findCurrentUserTransactions(TransactionFilter filter, Pageable pageable);

    TransactionResponse createTransactionForCurrentUser(UUID accountId, TransactionRequest transactionRequest);

    TransactionResponse updateTransactionForCurrentUser(UUID transactionId, TransactionUpdateRequest request);

    void deleteCurrentUserTransaction(UUID transactionId);

    byte[] generateCSVWithCurrentUserTransactions();

    TransactionResponse addImageToCurrentUserTransaction(UUID transactionId, MultipartFile file);

    TransactionResponse deleteImageFromCurrentUserTransaction(UUID transactionId);
}
