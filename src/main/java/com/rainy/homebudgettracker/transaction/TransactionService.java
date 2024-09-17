package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    Page<TransactionResponse> findAllByCurrentUserAndAccount(CurrencyCode currencyCode, Pageable pageable)
            throws RecordDoesNotExistException;
    Page<TransactionResponse> findAllByCurrentUserAndAccountAndCategory(
            CurrencyCode currencyCode, CategoryRequest categoryName, Pageable pageable
    ) throws RecordDoesNotExistException;
    Page<TransactionResponse> findAllByCurrentUserAndAccountAndDateBetween(
            CurrencyCode currencyCode,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) throws RecordDoesNotExistException;
    Page<TransactionResponse> findAllByCurrentUserAndAccountAndCategoryAndDateBetween(
            CurrencyCode currencyCode,
            String categoryName,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) throws RecordDoesNotExistException;
    TransactionResponse createTransactionForCurrentUser(TransactionRequest transactionRequest)
            throws RecordDoesNotExistException;
    TransactionResponse createTransactionForCurrentUser(CurrencyCode targetCurrency, BigDecimal exchangeRate,
                                                        TransactionRequest transactionRequest
    ) throws RecordDoesNotExistException;
    void deleteCurrentUserTransaction(Long transactionId)
            throws RecordDoesNotExistException, UserIsNotOwnerException;
    SumResponse sumPositiveAmountByCurrentUserAndAccount(CurrencyCode currencyCode)
            throws RecordDoesNotExistException;
    SumResponse sumNegativeAmountByCurrentUserAndAccount(CurrencyCode currencyCode)
            throws RecordDoesNotExistException;
    SumResponse sumAmountByCurrentUserAndAccount(CurrencyCode currencyCode) throws RecordDoesNotExistException;
    List<TransactionResponse> findAllByCurrentUser();
    byte[] generateCsvFileForCurrentUserTransactions() throws IOException;
}
