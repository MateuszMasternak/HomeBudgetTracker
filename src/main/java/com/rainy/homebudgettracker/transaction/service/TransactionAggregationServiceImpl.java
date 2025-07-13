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
import com.rainy.homebudgettracker.user.DefaultCurrencyResponseRequest;
import com.rainy.homebudgettracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    private final Map<LocalDate, BigDecimal> rates = new HashMap<>();

    @Override
    public SumResponse sumCurrentUserPositiveAmount(UUID accountId) {
        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumPositiveAmountByAccount(account), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserNegativeAmount(UUID accountId) {
        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumNegativeAmountByAccount(account), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserAmount(UUID accountId) {
        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumAmountByAccount(account), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserPositiveAmount(UUID accountId, LocalDate startDate, LocalDate endDate) {
        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumPositiveAmountByAccountAndDateBetween(
                account, startDate, endDate), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserPositiveAmount(
            UUID accountId, CategoryRequest categoryName, LocalDate startDate, LocalDate endDate) {
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
    public SumResponse sumCurrentUserNegativeAmount(UUID accountId, LocalDate startDate, LocalDate endDate) {
        Account account = accountService.findCurrentUserAccount(accountId);
        BigDecimal sum = normalize(transactionRepository.sumNegativeAmountByAccountAndDateBetween(
                account, startDate, endDate), 2);
        SumResponse response = modelMapper.map(sum, SumResponse.class);
        response.setAccount(modelMapper.map(account, AccountResponse.class));

        return response;
    }

    @Override
    public SumResponse sumCurrentUserNegativeAmount(
            UUID accountId, CategoryRequest categoryName, LocalDate startDate, LocalDate endDate) {
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
    ) {
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

    private BigDecimal convertTransactionAmountToDefaultCurrency(
            Transaction transaction,
            CurrencyCode defaultCurrency
    ) {
        ExchangeResponse exchangeResponse;
        BigDecimal rate;
        if (rates.containsKey(transaction.getDate())) {
            rate = rates.get(transaction.getDate());
        } else {
            CurrencyCode accountCurrency = transaction.getAccount().getCurrencyCode();
            if (transaction.getDate().isBefore(LocalDate.now())) {
                exchangeResponse = exchangeService.getHistoricalExchangeRate(
                        accountCurrency,
                        defaultCurrency,
                        transaction.getDate());
            } else {
                exchangeResponse = exchangeService.getExchangeRate(
                        accountCurrency,
                        defaultCurrency);
            }
            rate = BigDecimal.valueOf(Double.parseDouble(exchangeResponse.getConversionRate()));
            rates.put(transaction.getDate(), rate);
        }

        return transaction.getAmount().multiply(rate);
    }

    private CurrencyCode getDefaultCurrency() {
        DefaultCurrencyResponseRequest defaultCurrencyResponse = userService.getDefaultCurrency();
        return CurrencyCode.valueOf(defaultCurrencyResponse.getCurrencyCode());
    }

    @Override
    public SumResponse sumCurrentUserTotalAmountInDefaultCurrency() {
        List<BigDecimal> sums = new ArrayList<>();
        accountService.findCurrentUserAccounts()
                .forEach(account -> {
                    List<Transaction> transactions = (List<Transaction>) transactionRepository.findAllByAccount(account);
                    CurrencyCode defaultCurrency = getDefaultCurrency();
                    if (account.getCurrencyCode().name().equals(defaultCurrency.name())) {
                        sums.add(normalize(transactionRepository.sumAmountByAccount(account), 2));
                    } else {
                        BigDecimal sum = BigDecimal.ZERO;
                        for (Transaction transaction : transactions) {
                            sum = sum.add(convertTransactionAmountToDefaultCurrency(
                                    transaction,
                                    defaultCurrency));
                        }
                        sums.add(normalize(sum, 2));
                    }
                });

        BigDecimal totalSum = normalize(sums.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add), 2);

        return modelMapper.map(totalSum, SumResponse.class);
    }

    private BigDecimal getSumOfConvertedTransactions(
            List<Transaction> transactions,
            CurrencyCode defaultCurrency
    ) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (transaction.getAccount().getCurrencyCode() == defaultCurrency) {
                sum = normalize(sum.add(transaction.getAmount()), 2);
            } else {
                sum = normalize(sum.add(convertTransactionAmountToDefaultCurrency(
                        transaction,
                        defaultCurrency)), 2);
            }
        }

        return sum;
    }

    private SumResponse mapToSumResponse(Category category, BigDecimal sum) {
        SumResponse sumResponse = modelMapper.map(sum, SumResponse.class);
        sumResponse.setCategory(modelMapper.map(category, CategoryResponse.class));
        return sumResponse;
    }

    private List<SumResponse> getTopFiveAsResponse(Map<Category, BigDecimal> sums, boolean isNegative) {
        Comparator<Map.Entry<Category, BigDecimal>> comparator = Map.Entry.comparingByValue();
        if (!isNegative) {
            comparator = comparator.reversed();
        }

        return sums.entrySet().stream()
                .sorted(comparator)
                .limit(5)
                .map(entry -> {
                    Category category = entry.getKey();
                    BigDecimal sum = entry.getValue();
                    if (!isNegative && sum.compareTo(BigDecimal.ZERO) > 0) {
                        return mapToSumResponse(category, sum);
                    } else if (isNegative && sum.compareTo(BigDecimal.ZERO) < 0) {
                        return mapToSumResponse(category, sum);
                    } else {
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<SumResponse> getCurrentUserTopFiveIncomesConvertedToDefaultCurrency(
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Category> categories = categoryService.findCurrentUserCategories();
        Map<Category, BigDecimal> sums = new HashMap<>();
        categories.forEach(category -> {
            List<Transaction> transactions = (List<Transaction>) transactionRepository
                    .findAllPositiveByUserSubAndCategoryAndDateBetween(
                    userService.getUserSub(),
                    category,
                    startDate,
                    endDate);

            CurrencyCode defaultCurrency = getDefaultCurrency();

            BigDecimal sum = getSumOfConvertedTransactions(transactions, defaultCurrency);
            sums.put(category, sum);
        });

        return getTopFiveAsResponse(sums, false);
    }


    @Override
    public List<SumResponse> getCurrentUserTopFiveExpensesConvertedToDefaultCurrency(
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Category> categories = categoryService.findCurrentUserCategories();
        Map<Category, BigDecimal> sums = new HashMap<>();
        categories.forEach(category -> {
            List<Transaction> transactions = (List<Transaction>) transactionRepository
                    .findAllNegativeByUserSubAndCategoryAndDateBetween(
                    userService.getUserSub(),
                    category,
                    startDate,
                    endDate);

            CurrencyCode defaultCurrency = getDefaultCurrency();

            BigDecimal sum = getSumOfConvertedTransactions(transactions, defaultCurrency);
            sums.put(category, sum);
        });

        return getTopFiveAsResponse(sums, true);
    }

    @Override
    public SumResponse sumCurrentUserPositiveAmountInDefaultCurrency(LocalDate startDate, LocalDate endDate) {
        BigDecimal sum = BigDecimal.ZERO;
        List<Account> accounts = accountService.findCurrentUserAccounts();
        for (Account account : accounts) {
            List<Transaction> transactions = (List<Transaction>) transactionRepository
                    .findAllPositiveByUserSubAndAccountAndDateBetween(
                            userService.getUserSub(),
                            account,
                            startDate,
                            endDate);

            CurrencyCode defaultCurrency = getDefaultCurrency();

            sum = normalize(sum.add(getSumOfConvertedTransactions(transactions, defaultCurrency)), 2);
        }

        return modelMapper.map(sum, SumResponse.class);
    }

    @Override
    public SumResponse sumCurrentUserNegativeAmountInDefaultCurrency(LocalDate startDate, LocalDate endDate) {
        BigDecimal sum = BigDecimal.ZERO;
        List<Account> accounts = accountService.findCurrentUserAccounts();
        for (Account account : accounts) {
            List<Transaction> transactions = (List<Transaction>) transactionRepository
                    .findAllNegativeByUserSubAndAccountAndDateBetween(
                            userService.getUserSub(),
                            account,
                            startDate,
                            endDate);

            CurrencyCode defaultCurrency = getDefaultCurrency();

            sum = normalize(sum.add(getSumOfConvertedTransactions(transactions, defaultCurrency)), 2);
        }

        return modelMapper.map(sum, SumResponse.class);
    }
}
