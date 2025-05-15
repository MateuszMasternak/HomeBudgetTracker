package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.transaction.SumResponse;
import com.rainy.homebudgettracker.transaction.enums.PeriodType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TransactionAggregationService {
    SumResponse sumCurrentUserPositiveAmount(UUID accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    SumResponse sumCurrentUserNegativeAmount(UUID accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    SumResponse sumCurrentUserAmount(UUID accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    SumResponse sumCurrentUserPositiveAmount(UUID accountId, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    SumResponse sumCurrentUserPositiveAmount(
            UUID accountId, CategoryRequest categoryName, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    SumResponse sumCurrentUserNegativeAmount(UUID accountId, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    SumResponse sumCurrentUserNegativeAmount(
            UUID accountId, CategoryRequest categoryName, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    List<SumResponse> sumCurrentUserAmountInPeriod(UUID accountId, LocalDate date, PeriodType periodType)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    SumResponse sumCurrentUserTotalAmountInDefaultCurrency()
            throws UserIsNotOwnerException;

    List<SumResponse> getCurrentUserTopFiveIncomesConvertedToDefaultCurrency(
            LocalDate startDate, LocalDate endDate)
            throws UserIsNotOwnerException;

    List<SumResponse> getCurrentUserTopFiveExpensesConvertedToDefaultCurrency(
            LocalDate startDate, LocalDate endDate)
            throws UserIsNotOwnerException;

    public SumResponse sumCurrentUserPositiveAmountInDefaultCurrency(LocalDate startDate, LocalDate endDate);

    public SumResponse sumCurrentUserNegativeAmountInDefaultCurrency(LocalDate startDate, LocalDate endDate);
}
