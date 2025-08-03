package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.repository.TransactionRepository;
import com.rainy.homebudgettracker.transaction.service.TransactionAggregationServiceImpl;
import com.rainy.homebudgettracker.transaction.service.queryfilter.AggregationFilter;
import com.rainy.homebudgettracker.transaction.service.queryfilter.TransactionSpecificationBuilder;
import com.rainy.homebudgettracker.user.DefaultCurrencyResponseRequest;
import com.rainy.homebudgettracker.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionAggregationServiceTest {

    @InjectMocks
    private TransactionAggregationServiceImpl aggregationService;

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserService userService;
    @Mock
    private ExchangeService exchangeService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private TransactionSpecificationBuilder transactionSpecificationBuilder;

    @Nested
    @DisplayName("Get Sum Tests")
    class GetSumTests {

        @Test
        @DisplayName("should return sum of transactions without currency conversion")
        void getSum_shouldReturnSum_withoutConversion() {
            // Arrange
            String userSub = TestData.USER_SUB;
            AggregationFilter filter = new AggregationFilter(
                    TestData.ACCOUNT.getId(), null, null, null, null, false, false
            );
            List<Transaction> transactions = List.of(
                    TestData.TRANSACTION,
                    TestData.TRANSACTION_2
            );

            when(userService.getUserSub()).thenReturn(userSub);
            when(transactionSpecificationBuilder.build(filter, userSub)).thenReturn(Specification.where(null));
            when(transactionRepository.findAll(any(Specification.class))).thenReturn(transactions);

            when(modelMapper.map(any(BigDecimal.class), eq(SumResponse.class)))
                    .thenAnswer(inv -> SumResponse.builder()
                            .amount(inv.getArgument(0).toString())
                            .build());

            SumResponse result = aggregationService.getSum(filter);

            assertThat(result.amount()).isEqualTo("400.00");
        }

        @Test
        @DisplayName("should return sum of transactions with historical currency conversion")
        void getSum_shouldReturnSum_withHistoricalConversion() {
            String userSub = TestData.USER_SUB;
            CurrencyCode defaultCurrency = CurrencyCode.PLN;
            AggregationFilter filter = new AggregationFilter(
                    null, null, null, null, null, true, true
            );

            Transaction usdTransaction = TestData.TRANSACTION;
            Transaction plnTransaction = TestData.TRANSACTION_3;
            List<Transaction> transactions = List.of(usdTransaction, plnTransaction);

            when(userService.getUserSub()).thenReturn(userSub);
            when(userService.getDefaultCurrency()).thenReturn(DefaultCurrencyResponseRequest.builder()
                    .currencyCode(defaultCurrency.name())
                    .build());
            when(transactionSpecificationBuilder.build(filter, userSub)).thenReturn(Specification.where(null));
            when(transactionRepository.findAll(any(Specification.class))).thenReturn(transactions);

            when(exchangeService.getHistoricalExchangeRate(eq(CurrencyCode.USD), eq(CurrencyCode.PLN), any()))
                    .thenReturn(ExchangeResponse.builder()
                            .baseCode(CurrencyCode.USD.toString())
                            .targetCode(CurrencyCode.PLN.toString())
                            .conversionRate("4.00")
                            .build());

            when(modelMapper.map(any(BigDecimal.class), eq(SumResponse.class)))
                    .thenAnswer(inv -> SumResponse.builder()
                            .amount(inv.getArgument(0).toString())
                            .build());

            SumResponse result = aggregationService.getSum(filter);

            assertThat(result.amount()).isEqualTo("600.00");
        }

        @Test
        @DisplayName("should return zero when no transactions are found")
        void getSum_shouldReturnZero_whenNoTransactionsFound() {
            String userSub = TestData.USER_SUB;
            AggregationFilter filter = new AggregationFilter(
                    TestData.ACCOUNT.getId(), null, null, null, null, false, false
            );

            when(userService.getUserSub()).thenReturn(userSub);
            when(transactionSpecificationBuilder.build(filter, userSub)).thenReturn(Specification.where(null));
            when(transactionRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());
            when(modelMapper.map(any(BigDecimal.class), eq(SumResponse.class)))
                    .thenAnswer(inv -> SumResponse.builder()
                            .amount("0.00")
                            .build());

            SumResponse result = aggregationService.getSum(filter);

            assertThat(result.amount()).isEqualTo("0.00");
        }
    }
}
