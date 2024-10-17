package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.handler.exception.ImageUploadException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.handler.exception.WrongFileTypeException;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            Long accountId, Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            Long accountId, CategoryRequest categoryName, Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            Long accountId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    Page<TransactionResponse> findCurrentUserTransactionsAsResponses(
            Long accountId,
            CategoryRequest categoryName,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    TransactionResponse createTransactionForCurrentUser(Long accountId, TransactionRequest transactionRequest)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    TransactionResponse createTransactionForCurrentUser(
            Long accountId,
            CurrencyCode targetCurrency,
            BigDecimal exchangeRate,
            TransactionRequest transactionRequest)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    void deleteCurrentUserTransaction(Long transactionId)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    List<TransactionResponse> findCurrentUserTransactionsAsResponses();

    byte[] generateCSVWithCurrentUserTransactions()
            throws IOException;

    TransactionResponse addImageToCurrentUserTransaction(Long id, MultipartFile file)
            throws RecordDoesNotExistException, UserIsNotOwnerException, ImageUploadException, WrongFileTypeException;

    TransactionResponse deleteImageFromCurrentUserTransaction(Long id)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    SumResponse sumCurrentUserPositiveAmount(Long accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    SumResponse sumCurrentUserNegativeAmount(Long accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    SumResponse sumCurrentUserAmount(Long accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException;
}
