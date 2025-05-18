package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.handler.exception.*;
import com.rainy.homebudgettracker.transaction.SumResponse;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.TransactionUpdateRequest;
import com.rainy.homebudgettracker.transaction.enums.PeriodType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            UUID accountId, Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            UUID accountId, CategoryRequest categoryName, Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            UUID accountId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            UUID accountId,
            CategoryRequest categoryName,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    TransactionResponse createTransactionForCurrentUser(UUID accountId, TransactionRequest transactionRequest)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    TransactionResponse createTransactionForCurrentUser(
            UUID accountId,
            BigDecimal exchangeRate,
            TransactionRequest transactionRequest)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    void deleteCurrentUserTransaction(UUID transactionId)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    List<TransactionResponse> findCurrentUserTransactionsAsResponses();

    byte[] generateCSVWithCurrentUserTransactions()
            throws IOException;

    TransactionResponse addImageToCurrentUserTransaction(UUID id, MultipartFile file)
            throws RecordDoesNotExistException, UserIsNotOwnerException, ImageUploadException, WrongFileTypeException, PremiumStatusRequiredException;

    TransactionResponse deleteImageFromCurrentUserTransaction(UUID id)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    TransactionResponse updateTransactionForCurrentUser(UUID transactionId, TransactionUpdateRequest request)
            throws RecordDoesNotExistException, UserIsNotOwnerException;
}
