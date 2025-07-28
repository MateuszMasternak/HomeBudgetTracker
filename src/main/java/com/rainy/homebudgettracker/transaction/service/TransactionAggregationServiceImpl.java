package com.rainy.homebudgettracker.transaction.service;

import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.*;
import com.rainy.homebudgettracker.transaction.enums.AmountType;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.service.queryfilter.AggregationFilter;
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

        BigDecimal totalSum = filter.convertToDefaultCurrency()
                ? sumWithCurrencyConversion(transactions, CurrencyCode.valueOf(
                        userService.getDefaultCurrency().getCurrencyCode()))
                : sumWithoutConversion(transactions);

        return modelMapper.map(normalize(totalSum, 2), SumResponse.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SumResponse> getTopFiveIncomes(AggregationFilter filter) {
        var newFilter = new AggregationFilter(
                filter.accountId(), filter.categoryId(), filter.startDate(), filter.endDate(),
                filter.amountType(), filter.convertToDefaultCurrency()
        );
        return getTopFive(newFilter, Map.Entry.comparingByValue(Comparator.reverseOrder()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SumResponse> getTopFiveExpenses(AggregationFilter filter) {
        var newFilter = new AggregationFilter(
                filter.accountId(), filter.categoryId(), filter.startDate(), filter.endDate(),
                filter.amountType(), filter.convertToDefaultCurrency()
        );
        return getTopFive(newFilter, Map.Entry.comparingByValue());
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
                        entry -> filter.convertToDefaultCurrency()
                                ? sumWithCurrencyConversion(entry.getValue(), defaultCurrency)
                                : sumWithoutConversion(entry.getValue())
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

    private BigDecimal sumWithCurrencyConversion(List<Transaction> transactions, CurrencyCode defaultCurrency) {
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
                    amount = amount.multiply(rate);
                } else {
                    log.warn("Could not find exchange rate for query: {}", query);
                }
            }
            total = total.add(amount);
        }
        return total;
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
        return new BigDecimal(response.getConversionRate());
    }
}
