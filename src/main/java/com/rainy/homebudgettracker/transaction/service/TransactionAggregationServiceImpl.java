package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.exchange.CurrencyConverter;
import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.*;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.PeriodType;
import com.rainy.homebudgettracker.transaction.service.queryfilter.AggregationFilter;
import com.rainy.homebudgettracker.transaction.service.queryfilter.PeriodicAggregationFilter;
import com.rainy.homebudgettracker.transaction.service.queryfilter.TransactionSpecificationBuilder;
import com.rainy.homebudgettracker.transaction.repository.TransactionRepository;
import com.rainy.homebudgettracker.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rainy.homebudgettracker.transaction.service.helper.BigDecimalNormalization.normalize;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAggregationServiceImpl implements TransactionAggregationService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final ExchangeService exchangeService;
    private final ModelMapper modelMapper;
    private final TransactionSpecificationBuilder transactionSpecificationBuilder;

    private record RateQuery(LocalDate date, CurrencyCode from, CurrencyCode to) {}

    @Override
    @Transactional(readOnly = true)
    public SumResponse getSum(AggregationFilter filter) {
        String userSub = userService.getUserSub();
        Specification<Transaction> spec = transactionSpecificationBuilder.build(filter, userSub);
        List<Transaction> transactions = transactionRepository.findAll(spec);

        BigDecimal totalSum;
        if (filter.convertToDefaultCurrency()) {
            CurrencyCode defaultCurrency = CurrencyCode.valueOf(userService.getDefaultCurrency().getCurrencyCode());
            // ZMIANA: Wybieramy metodę konwersji na podstawie flagi `historical`
            totalSum = filter.historicalConversion()
                    ? sumWithHistoricalConversion(transactions, defaultCurrency)
                    : sumWithCurrentRateConversion(transactions, defaultCurrency);
        } else {
            totalSum = sumWithoutConversion(transactions);
        }

        return modelMapper.map(normalize(totalSum, 2), SumResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SumResponse> getTopFiveIncomes(AggregationFilter filter) {
        return getTopFive(filter, Map.Entry.comparingByValue(Comparator.reverseOrder()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SumResponse> getTopFiveExpenses(AggregationFilter filter) {
        return getTopFive(filter, Map.Entry.comparingByValue());
    }

    private List<SumResponse> getTopFive(AggregationFilter filter, Comparator<Map.Entry<Category, BigDecimal>> comparator) {
        String userSub = userService.getUserSub();
        CurrencyCode defaultCurrency = CurrencyCode.valueOf(userService.getDefaultCurrency().getCurrencyCode());

        Specification<Transaction> spec = transactionSpecificationBuilder.build(filter, userSub);
        List<Transaction> transactions = transactionRepository.findAll(spec);

        Map<Category, List<Transaction>> groupedByCategory = transactions.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(Transaction::getCategory));

        Map<Category, BigDecimal> categorySums = groupedByCategory.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            if (filter.convertToDefaultCurrency()) {
                                return filter.historicalConversion()
                                        ? sumWithHistoricalConversion(entry.getValue(), defaultCurrency)
                                        : sumWithCurrentRateConversion(entry.getValue(), defaultCurrency);
                            } else {
                                return sumWithoutConversion(entry.getValue());
                            }
                        }
                ));

        return categorySums.entrySet().stream()
                .sorted(comparator)
                .limit(5)
                .map(entry -> modelMapper.map(entry.getValue(), SumResponse.class, entry.getKey()))
                .collect(Collectors.toList());
    }

    private BigDecimal sumWithoutConversion(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumWithCurrentRateConversion(List<Transaction> transactions, CurrencyCode defaultCurrency) {
        if (transactions == null || transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<CurrencyCode, BigDecimal> sumsByCurrency = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getAccount().getCurrencyCode(),
                        Collectors.mapping(Transaction::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<CurrencyCode, BigDecimal> entry : sumsByCurrency.entrySet()) {
            CurrencyCode sourceCurrency = entry.getKey();
            BigDecimal amountInSourceCurrency = entry.getValue();

            if (sourceCurrency == defaultCurrency) {
                total = total.add(amountInSourceCurrency);
            } else {
                BigDecimal currentRate = getCurrencyRate(sourceCurrency, defaultCurrency, LocalDate.now());
                BigDecimal convertedAmount = CurrencyConverter.convert(amountInSourceCurrency, currentRate, 4);
                total = total.add(convertedAmount);
            }
        }
        return normalize(total, 2);
    }

    private BigDecimal sumWithHistoricalConversion(List<Transaction> transactions, CurrencyCode defaultCurrency) {
        if (transactions == null || transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<RateQuery, BigDecimal> ratesCache = prefetchExchangeRates(transactions, defaultCurrency);
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            BigDecimal amount = t.getAmount();
            CurrencyCode sourceCurrency = t.getAccount().getCurrencyCode();
            if (sourceCurrency != defaultCurrency) {
                RateQuery query = new RateQuery(t.getDate(), sourceCurrency, defaultCurrency);
                BigDecimal rate = ratesCache.get(query);
                if (rate != null) {
                    amount = CurrencyConverter.convert(amount, rate, 4);
                } else {
                    log.warn("Could not find exchange rate for query: {}", query);
                }
            }
            total = total.add(amount);
        }
        return normalize(total, 2);
    }

    private Map<RateQuery, BigDecimal> prefetchExchangeRates(List<Transaction> transactions, CurrencyCode defaultCurrency) {
        return transactions.stream()
                .filter(t -> t.getAccount().getCurrencyCode() != defaultCurrency)
                .map(t -> new RateQuery(t.getDate(), t.getAccount().getCurrencyCode(), defaultCurrency))
                .distinct()
                .collect(Collectors.toMap(
                        query -> query,
                        query -> getCurrencyRate(query.from, query.to, query.date)
                ));
    }

    private BigDecimal getCurrencyRate(CurrencyCode from, CurrencyCode to, LocalDate date) {
        ExchangeResponse response = date.isBefore(LocalDate.now())
                ? exchangeService.getHistoricalExchangeRate(from, to, date)
                : exchangeService.getExchangeRate(from, to);
        return new BigDecimal(response.conversionRate());
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceHistoryResponse getBalanceHistory(PeriodicAggregationFilter filter) {
        String userSub = userService.getUserSub();

        LocalDate initialBalanceEndDate = filter.periodType() == PeriodType.YEAR
                ? filter.date().withDayOfYear(1).minusDays(1)
                : filter.date().withDayOfMonth(1).minusDays(1);

        AggregationFilter initialBalanceFilter = new AggregationFilter(
                filter.accountId(), null, null, initialBalanceEndDate, null,
                false, false);
        Specification<Transaction> initialSpec = transactionSpecificationBuilder.build(initialBalanceFilter, userSub);
        BigDecimal initialBalance = sumWithoutConversion(transactionRepository.findAll(initialSpec));

        LocalDate periodStartDate = filter.periodType() == PeriodType.YEAR
                ? filter.date().withDayOfYear(1)
                : filter.date().withDayOfMonth(1);
        LocalDate periodEndDate = filter.periodType() == PeriodType.YEAR
                ? filter.date().withDayOfYear(filter.date().lengthOfYear())
                : filter.date().withDayOfMonth(filter.date().lengthOfMonth());

        AggregationFilter periodFilter = new AggregationFilter(
                filter.accountId(), null, periodStartDate, periodEndDate, null,
                false, false);
        Specification<Transaction> periodSpec = transactionSpecificationBuilder.build(periodFilter, userSub);
        List<Transaction> transactionsInPeriod = transactionRepository.findAll(periodSpec);

        Map<Integer, BigDecimal> deltas = groupAndSumTransactions(transactionsInPeriod, filter.periodType());

        int numberOfPeriods = filter.periodType() == PeriodType.YEAR ? 12 : periodEndDate.getDayOfMonth();
        List<BigDecimal> periodicDeltas = new ArrayList<>();
        for (int i = 1; i <= numberOfPeriods; i++) {
            periodicDeltas.add(deltas.getOrDefault(i, BigDecimal.ZERO));
        }

        return new BalanceHistoryResponse(normalize(initialBalance, 2), periodicDeltas);
    }

    private Map<Integer, BigDecimal> groupAndSumTransactions(List<Transaction> transactions, PeriodType periodType) {
        Function<Transaction, Integer> grouper = periodType == PeriodType.YEAR
                ? t -> t.getDate().getMonthValue()
                : t -> t.getDate().getDayOfMonth();

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        grouper,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }
}
