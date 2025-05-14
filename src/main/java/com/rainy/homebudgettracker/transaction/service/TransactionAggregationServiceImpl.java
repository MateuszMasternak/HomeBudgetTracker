package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.account.AccountService;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.SumResponse;
import com.rainy.homebudgettracker.transaction.Transaction;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.PeriodType;
import com.rainy.homebudgettracker.user.DefaultCurrency;
import com.rainy.homebudgettracker.user.DefaultCurrencyResponseRequest;
import com.rainy.homebudgettracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static com.rainy.homebudgettracker.transaction.BigDecimalNormalization.normalize;

@Service
@RequiredArgsConstructor
public class TransactionAggregationServiceImpl implements TransactionAggregationService {
    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final ExchangeService exchangeService;
    private final UserService userService;

    @Override
    public SumResponse sumCurrentUserPositiveAmount(UUID accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumPositiveAmountByAccount(account), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserNegativeAmount(UUID accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumNegativeAmountByAccount(account), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserAmount(UUID accountId)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumAmountByAccount(account), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserPositiveAmount(UUID accountId, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumPositiveAmountByAccountAndDateBetween(
                account, startDate, endDate), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserPositiveAmount(UUID accountId, CategoryRequest categoryName, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        Category category = categoryService.findCurrentUserCategory(categoryName.getName());
        BigDecimal sum = normalize(transactionRepository.sumPositiveAmountByAccountAndCategoryAndDateBetween(
                account, category, startDate, endDate), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));
        response.setCategory(modelMapper.map(category, CategoryResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserNegativeAmount(UUID accountId, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumNegativeAmountByAccountAndDateBetween(
                account, startDate, endDate), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserNegativeAmount(UUID accountId, CategoryRequest categoryName, LocalDate startDate, LocalDate endDate)
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);
        Category category = categoryService.findCurrentUserCategory(categoryName.getName());
        BigDecimal sum = normalize(transactionRepository.sumNegativeAmountByAccountAndCategoryAndDateBetween(
                account, category, startDate, endDate), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));
        response.setCategory(modelMapper.map(category, CategoryResponse.class));

        return response;
    }

    @Override
    public List<SumResponse> sumCurrentUserAmountInPeriod(
            UUID accountId,
            LocalDate date,
            PeriodType periodType
    ) throws RecordDoesNotExistException, UserIsNotOwnerException {

        Account account = accountService.findCurrentUserAccount(accountId);

        LocalDate dateToSumBeforeAmounts = getDateBeforePeriod(periodType, date);
        BigDecimal total = normalize(transactionRepository.sumAmountByAccountToDate(
                account, dateToSumBeforeAmounts), 2);

        return getSumResponses(account, date, periodType, total);
    }

    private int getPeriodCount(PeriodType periodType, LocalDate date) {
        return switch (periodType) {
            case MONTH -> date.getMonth().length(date.isLeapYear());
            case YEAR -> 12;
        };
    }

    private LocalDate getDateBeforePeriod(PeriodType periodType, LocalDate date) {
        return switch (periodType) {
            case MONTH -> date.minusMonths(1)
                    .withDayOfMonth(date.minusMonths(1).lengthOfMonth());
            case YEAR -> date.minusYears(1).withMonth(12).withDayOfMonth(31);
        };
    }

    private List<SumResponse> getSumResponses(
            Account account,
            LocalDate date,
            PeriodType periodType,
            BigDecimal total
    ) {

        List<SumResponse> sumResponses = new ArrayList<>();

        for (int i = 1; i < getPeriodCount(periodType, date) + 1; i++) {

            total = total.add(normalize(getSumInPeriod(account, date, periodType, i), 2));

            SumResponse response = modelMapper.map(total, SumResponse.class);
            response.setAccount(modelMapper.map(account, AccountResponse.class));
            sumResponses.add(response);
        }

        return sumResponses;
    }

    private BigDecimal getSumInPeriod(
            Account account,
            LocalDate date,
            PeriodType periodType,
            int periodCount
    ) {
        BigDecimal sum;
        if (periodType == PeriodType.MONTH) {
            LocalDate searchedDay = date.withDayOfMonth(periodCount);
            sum = transactionRepository.sumAmountByAccountAndDateBetween(
                    account, searchedDay, searchedDay);
        } else {
            LocalDate startDate = date.withMonth(periodCount).withDayOfMonth(1);
            int daysCount = startDate.getMonth().length(startDate.isLeapYear());
            LocalDate endDate = date.withMonth(periodCount).withDayOfMonth(daysCount);
            sum = transactionRepository.sumAmountByAccountAndDateBetween(
                    account, startDate, endDate);
        }

        return normalize(sum, 2);
    }

    @Override
    public SumResponse sumCurrentUserTotalAmountInDefaultCurrency() {
        List<BigDecimal> sums = new ArrayList<>();
        accountService.findCurrentUserAccounts()
                .forEach(account -> {
                    List<Transaction> transactions = (List<Transaction>) transactionRepository.findAllByAccount(account);
                    DefaultCurrencyResponseRequest defaultCurrency = userService.getDefaultCurrency();
                    if (account.getCurrencyCode().name().equals(defaultCurrency.getCurrencyCode())) {
                        sums.add(normalize(transactionRepository.sumAmountByAccount(account), 2));
                    } else {
                        Map<LocalDate, BigDecimal> rates = new HashMap<>();
                        BigDecimal sum = BigDecimal.ZERO;
                        for (Transaction transaction : transactions) {
                            ExchangeResponse exchangeResponse;
                            BigDecimal rate;
                            if (rates.containsKey(transaction.getDate())) {
                                rate = rates.get(transaction.getDate());
                            } else {
                                if (transaction.getDate().isBefore(LocalDate.now())) {
                                    exchangeResponse = exchangeService.getHistoricalExchangeRate(
                                            account.getCurrencyCode(),
                                            CurrencyCode.valueOf(defaultCurrency.getCurrencyCode()),
                                            transaction.getDate());
                                } else {
                                    exchangeResponse = exchangeService.getExchangeRate(
                                            account.getCurrencyCode(),
                                            CurrencyCode.valueOf(defaultCurrency.getCurrencyCode()));
                                }
                                rate = BigDecimal.valueOf(Double.parseDouble(exchangeResponse.getConversionRate()));
                                rates.put(transaction.getDate(), rate);
                            }

                            sum = sum.add(transaction.getAmount().multiply(rate));
                        }
                        sums.add(normalize(sum, 2));
                    }
                });

        BigDecimal totalSum = normalize(sums.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add), 2);

        return modelMapper.map(totalSum, SumResponse.class);
    }
}
