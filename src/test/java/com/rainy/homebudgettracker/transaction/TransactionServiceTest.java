package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.account.AccountService;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.exchange.CurrencyConverter;
import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.PaymentMethod;
import com.rainy.homebudgettracker.user.Role;
import com.rainy.homebudgettracker.user.User;
import com.rainy.homebudgettracker.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {
    @InjectMocks
    TransactionServiceImpl transactionService;
    @Mock
    TransactionRepository transactionRepository;
    @Mock
    CategoryService categoryService;
    @Mock
    AccountService accountService;
    @Mock
    ExchangeService exchangeService;
    @Mock
    ModelMapper modelMapper;
    @Mock
    UserService userService;

    @BeforeEach
    void setUp() throws RecordDoesNotExistException, UserIsNotOwnerException {
        MockitoAnnotations.openMocks(this);

        User user = User.builder()
                .id(1L)
                .email("test@mail.com")
                .password("password")
                .role(Role.USER)
                .build();
        when(userService.getCurrentUser()).thenReturn(user);

        User user2 = User.builder()
                .id(2L)
                .email("test2@mail.com")
                .password("password")
                .role(Role.USER)
                .build();

        Account account = Account.builder()
                .id(1L)
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .user(user)
                .build();
        when(accountService.findCurrentUserAccount(1L)).thenReturn(account);
        when(accountService.findCurrentUserAccount(2L)).thenThrow(UserIsNotOwnerException.class);
        when(accountService.findCurrentUserAccount(3L)).thenThrow(RecordDoesNotExistException.class);
        when(modelMapper.map(account, AccountResponse.class)).thenReturn(AccountResponse.builder()
                .id(1L)
                .name("USD account")
                .currencyCode("USD")
                .build());

        Account account2 = Account.builder()
                .id(2L)
                .name("PLN account")
                .currencyCode(CurrencyCode.PLN)
                .user(user)
                .build();
        when(accountService.findCurrentUserAccount(4L)).thenReturn(account2);
        when(modelMapper.map(account2, AccountResponse.class)).thenReturn(AccountResponse.builder()
                .id(2L)
                .name("PLN account")
                .currencyCode("PLN")
                .build());

        Category category = Category.builder()
                .id(1L)
                .name("Food")
                .build();
        when(categoryService.findCurrentUserCategory("Food")).thenReturn(category);
        when(categoryService.findCurrentUserCategory("Healthcare")).thenThrow(RecordDoesNotExistException.class);

        Transaction transaction = Transaction.builder()
                .id(1L)
                .user(user)
                .amount(BigDecimal.valueOf(100, 2))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .category(category)
                .account(account)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(2L)
                .user(user2)
                .amount(BigDecimal.valueOf(100, 2))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .category(category)
                .account(account)
                .build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.findById(2L)).thenReturn(Optional.of(transaction2));
        when(transactionRepository.findById(3L)).thenReturn(Optional.empty());

        var pageable1 = PageRequest.of(0, 10);
        when(transactionRepository.findAllByAccount(account, pageable1)).thenReturn(new PageImpl<>(List.of(transaction)));
        when(transactionRepository.findAllByAccountAndCategory(account, category, pageable1)).thenReturn(new PageImpl<>(List.of(transaction)));
        when(transactionRepository.findAllByAccountAndDateBetween(account, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), pageable1)).thenReturn(new PageImpl<>(List.of(transaction)));
        when(transactionRepository.findAllByAccountAndCategoryAndDateBetween(account, category, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), pageable1)).thenReturn(new PageImpl<>(List.of(transaction)));

        var pageable2 = PageRequest.of(1, 10);
        when(transactionRepository.findAllByAccount(account, pageable2)).thenReturn(Page.empty());
        when(transactionRepository.findAllByAccountAndCategory(account, category, pageable2)).thenReturn(Page.empty());
        when(transactionRepository.findAllByAccountAndDateBetween(account, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), pageable2)).thenReturn(Page.empty());
        when(transactionRepository.findAllByAccountAndCategoryAndDateBetween(account, category, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), pageable2)).thenReturn(Page.empty());

        TransactionResponse transactionResponse = TransactionResponse.builder()
                .id(1L)
                .amount("100.00")
                .date("2024-01-01")
                .paymentMethod("CASH")
                .category(CategoryResponse.builder()
                        .id(1L)
                        .name("Food")
                        .build())
                .account(AccountResponse.builder()
                        .id(1L)
                        .name("USD account")
                        .currencyCode("USD")
                        .build())
                .build();
        when(modelMapper.map(transaction, TransactionResponse.class)).thenReturn(transactionResponse);

        TransactionRequest transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP))
                .categoryName(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.USD)
                .paymentMethod(PaymentMethod.CASH)
                .build();
        when(modelMapper.mapTransactionRequestToTransaction(transactionRequest, account, category)).thenReturn(transaction);

        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Transaction convertedTransaction = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .category(category)
                .account(account)
                .build();

        TransactionRequest convertedTransactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP))
                .categoryName(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.PLN)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        TransactionResponse convertedTransactionResponse = TransactionResponse.builder()
                .id(1L)
                .amount("421.00")
                .date("2024-01-01")
                .paymentMethod("CASH")
                .category(CategoryResponse.builder()
                        .id(1L)
                        .name("Food")
                        .build())
                .account(AccountResponse.builder()
                        .id(1L)
                        .name("PLN account")
                        .currencyCode("PLN")
                        .build())
                .build();

        when(modelMapper.mapTransactionRequestToTransaction(convertedTransactionRequest, account, category)).thenReturn(convertedTransaction);
        when(modelMapper.map(convertedTransaction, TransactionResponse.class)).thenReturn(convertedTransactionResponse);
        when(transactionRepository.save(convertedTransaction)).thenReturn(convertedTransaction);

        Transaction convertedTransaction_2 = Transaction.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .category(category)
                .account(account)
                .details("EUR->PLN: 4.21")
                .build();

        TransactionRequest convertedTransactionRequest_2 = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP))
                .categoryName(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.PLN)
                .paymentMethod(PaymentMethod.CASH)
                .details("EUR->PLN: 4.21")
                .build();

        TransactionResponse convertedTransactionResponse_2 = TransactionResponse.builder()
                .id(1L)
                .amount("421.00")
                .date("2024-01-01")
                .paymentMethod("CASH")
                .category(CategoryResponse.builder()
                        .id(1L)
                        .name("Food")
                        .build())
                .account(AccountResponse.builder()
                        .id(1L)
                        .name("PLN account")
                        .currencyCode("PLN")
                        .build())
                .details("EUR->PLN: 4.21")
                .build();

        when(modelMapper.mapTransactionRequestToTransaction(convertedTransactionRequest_2, account, category)).thenReturn(convertedTransaction_2);
        when(modelMapper.map(convertedTransaction_2, TransactionResponse.class)).thenReturn(convertedTransactionResponse_2);
        when(transactionRepository.save(convertedTransaction_2)).thenReturn(convertedTransaction_2);
        when(exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.PLN)).thenReturn(ExchangeResponse.builder().conversionRate("4.21").build());

        when(transactionRepository.sumPositiveAmountByAccount(account)).thenReturn(BigDecimal.valueOf(100.1));
        when(transactionRepository.sumPositiveAmountByAccount(account2)).thenReturn(BigDecimal.valueOf(0));
        when(transactionRepository.sumNegativeAmountByAccount(account)).thenReturn(BigDecimal.valueOf(100.1));
        when(transactionRepository.sumNegativeAmountByAccount(account2)).thenReturn(BigDecimal.valueOf(0));
        when(transactionRepository.sumAmountByAccount(account)).thenReturn(BigDecimal.valueOf(100.1));
        when(transactionRepository.sumAmountByAccount(account2)).thenReturn(BigDecimal.valueOf(0));

        SumResponse sumResponse = SumResponse.builder().amount("100.10").build();
        SumResponse sumResponse2 = SumResponse.builder().amount("0.00").build();
        when(modelMapper.map(BigDecimal.valueOf(100.1).setScale(2, RoundingMode.HALF_UP), SumResponse.class)).thenReturn(sumResponse);
        when(modelMapper.map(BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), SumResponse.class)).thenReturn(sumResponse2);

        when(transactionRepository.findAllByUser(user)).thenReturn(List.of(transaction));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnPageWithTransactionResponse() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var pageable = PageRequest.of(0, 10);
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses(1L, pageable);

        assertEquals(1, returnedTransactionResponses.getTotalElements());
    }

    @Test
    void shouldReturnEmptyPageWhenPageOneIsEmpty() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var pageable = PageRequest.of(1, 10);
        var transactionResponses = transactionService.findCurrentUserTransactionsAsResponses(1L, pageable);

        assertEquals(0, transactionResponses.getTotalElements());
    }

    @Test
    void shouldThrowExceptionWhenAccountBelongToAnotherUser() {
        var pageable = PageRequest.of(0, 10);

        assertThrows(UserIsNotOwnerException.class,
                () -> transactionService.findCurrentUserTransactionsAsResponses(2L, pageable));
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() {
        var pageable = PageRequest.of(0, 10);

        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findCurrentUserTransactionsAsResponses(3L, pageable));
    }

    @Test
    void shouldReturnPageWithTransactionResponseCategory() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var pageable = PageRequest.of(0, 10);
        var categoryRequest = CategoryRequest.builder().name("Food").build();
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses(
                1L, categoryRequest, pageable);

        assertEquals(1, returnedTransactionResponses.getTotalElements());
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        var pageable = PageRequest.of(0, 10);
        var categoryRequest = CategoryRequest.builder().name("Healthcare").build();

        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findCurrentUserTransactionsAsResponses(1L, categoryRequest, pageable));
    }


    @Test
    void shouldReturnTransactionResponseDateBetween() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses(
                1L, startDate, endDate, pageable);

        assertEquals(1, returnedTransactionResponses.getTotalElements());
    }

    @Test
    void shouldReturnEmptyPageWhenPageOneIsEmptyDateBetween()
            throws RecordDoesNotExistException,
            UserIsNotOwnerException {

        var pageable = PageRequest.of(1, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var transactionResponses = transactionService.findCurrentUserTransactionsAsResponses(
                1L, startDate, endDate, pageable);

        assertEquals(0, transactionResponses.getTotalElements());
    }

    @Test
    void shouldReturnTransactionResponseCategoryDateBetween()
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var categoryRequest = CategoryRequest.builder().name("Food").build();
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses(
               1L, categoryRequest, startDate, endDate, pageable);

        assertEquals(1, returnedTransactionResponses.getTotalElements());
    }

    @Test
    void shouldReturnTransactionResponseWhenTransactionIsCreated()
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP))
                .categoryName(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.USD)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        var returnedTransactionResponse = transactionService.createTransactionForCurrentUser(1L, transactionRequest);

        var transactionResponse = TransactionResponse.builder()
                .id(1L)
                .amount("100.00")
                .date("2024-01-01")
                .paymentMethod("CASH")
                .category(CategoryResponse.builder()
                        .id(1L)
                        .name("Food")
                        .build())
                .account(AccountResponse.builder()
                        .id(1L)
                        .name("USD account")
                        .currencyCode("USD")
                        .build())
                .build();

        assertEquals(transactionResponse, returnedTransactionResponse);
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist_Create() {

        var transactionRequest = TransactionRequest.builder()
                .categoryName(CategoryRequest.builder().name("Food").build())
                .build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.createTransactionForCurrentUser(3L, transactionRequest));

    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist_Create() {
        var transactionRequest = TransactionRequest.builder()
                .categoryName(CategoryRequest.builder().name("Healthcare").build())
                .build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.createTransactionForCurrentUser(1L, transactionRequest));
    }

    @Test
    void shouldThrowExceptionWhenAccountBelongToAnotherUser_Create() {
        var transactionRequest = TransactionRequest.builder()
                .categoryName(CategoryRequest.builder().name("Food").build())
                .build();
        assertThrows(UserIsNotOwnerException.class,
                () -> transactionService.createTransactionForCurrentUser(2L, transactionRequest));
    }

    @Test
    void shouldReturnTransactionResponseWhenTransactionIsCreated2()
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        try(MockedStatic<CurrencyConverter> converter = Mockito.mockStatic(CurrencyConverter.class)) {
            converter.when(() -> CurrencyConverter.convert(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP),
                            BigDecimal.valueOf(4.21).setScale(2, RoundingMode.HALF_UP),
                            2))
                    .thenReturn(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP));

            var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP))
                .categoryName(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.EUR)
                .paymentMethod(PaymentMethod.CASH)
                    .build();

            var returnedTransactionResponse = transactionService.createTransactionForCurrentUser(
                    1L, CurrencyCode.PLN, BigDecimal.valueOf(4.21), transactionRequest);

            var transactionResponse = TransactionResponse.builder()
                    .id(1L)
                    .amount("421.00")
                    .date("2024-01-01")
                    .paymentMethod("CASH")
                    .category(CategoryResponse.builder()
                            .id(1L)
                            .name("Food")
                            .build())
                    .account(AccountResponse.builder()
                            .id(1L)
                            .name("PLN account")
                            .currencyCode("PLN")
                            .build())
                    .build();

            assertEquals(transactionResponse, returnedTransactionResponse);
        }
    }

    @Test
    void shouldReturnTransactionResponseWhenTransactionIsCreated2NullExchangeRate()
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        try(MockedStatic<CurrencyConverter> converter = Mockito.mockStatic(CurrencyConverter.class)) {
            converter.when(() -> CurrencyConverter.convert(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP),
                            BigDecimal.valueOf(4.21).setScale(2, RoundingMode.HALF_UP),
                            2))
                    .thenReturn(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP));

            var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP))
                .categoryName(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.EUR)
                .paymentMethod(PaymentMethod.CASH)
                    .build();

            var returnedTransactionResponse = transactionService.createTransactionForCurrentUser(
                    1L, CurrencyCode.PLN, null, transactionRequest);

            var transactionResponse = TransactionResponse.builder()
                    .id(1L)
                    .amount("421.00")
                    .date("2024-01-01")
                    .paymentMethod("CASH")
                    .category(CategoryResponse.builder()
                            .id(1L)
                            .name("Food")
                            .build())
                    .account(AccountResponse.builder()
                            .id(1L)
                            .name("PLN account")
                            .currencyCode("PLN")
                            .build())
                    .details("EUR->PLN: 4.21")
                    .build();

            assertEquals(transactionResponse, returnedTransactionResponse);
        }
    }

    @Test
    void shouldDeleteTransaction() {
        assertDoesNotThrow(() -> transactionService.deleteCurrentUserTransaction(1L));
    }

    @Test
    void shouldThrowExceptionWhenTransactionDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.deleteCurrentUserTransaction(3L));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwnerWhenDeleting() {
        assertThrows(UserIsNotOwnerException.class,
                () -> transactionService.deleteCurrentUserTransaction(2L));
    }

    @Test
    void shouldReturnSumPositiveAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserPositiveAmount(1L);

        AccountResponse account = AccountResponse.builder()
                .id(1L)
                .name("USD account")
                .currencyCode("USD")
                .build();

        assertEquals(SumResponse.builder().amount("100.10").account(account).build(), sumPositiveAmount);
    }

    @Test
    void shouldReturnSumAs0PositiveAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserPositiveAmount(4L);

        AccountResponse account = AccountResponse.builder()
                .id(2L)
                .name("PLN account")
                .currencyCode("PLN")
                .build();
        assertEquals(SumResponse.builder().amount("0.00").account(account).build(), sumPositiveAmount);

    }

    @Test
    void shouldReturnSumNegativeAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumNegativeAmount = transactionService.sumCurrentUserNegativeAmount(1L);

        AccountResponse account = AccountResponse.builder()
                .id(1L)
                .name("USD account")
                .currencyCode("USD")
                .build();
        assertEquals(SumResponse.builder().amount("100.10").account(account).build(), sumNegativeAmount);
    }

    @Test
    void shouldReturnSumAs0NegativeAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserNegativeAmount(4L);

        AccountResponse account = AccountResponse.builder()
                .id(2L)
                .name("PLN account")
                .currencyCode("PLN")
                .build();
        assertEquals(SumResponse.builder().amount("0.00").account(account).build(), sumPositiveAmount);
    }

    @Test
    void shouldReturnSumAllAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumAllAmount = transactionService.sumCurrentUserAmount(1L);

        AccountResponse account = AccountResponse.builder()
                .id(1L)
                .name("USD account")
                .currencyCode("USD")
                .build();
        assertEquals(SumResponse.builder().amount("100.10").account(account).build(), sumAllAmount);
    }

    @Test
    void shouldReturnSumAs0AllAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserAmount(4L);

        AccountResponse account = AccountResponse.builder()
                .id(2L)
                .name("PLN account")
                .currencyCode("PLN")
                .build();
        assertEquals(SumResponse.builder().amount("0.00").account(account).build(), sumPositiveAmount);
    }

    @Test
    void shouldReturnListWithTransactionResponse() {
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses();

        assertEquals(1, returnedTransactionResponses.size());
    }

    // CSV EXPORT
    @Test
    void shouldReturnCsvString() throws IOException {
        var csv = transactionService.generateCSVWithCurrentUserTransactions();
        var expectedCsv = "sep=,\n" +
                "ID,Amount,Category,Date,Currency code,Payment method\n" +
                "1,100.00,Food,2024-01-01,USD,CASH\n";
        var expectedCsvAsBytes = expectedCsv.getBytes();

        assertArrayEquals(expectedCsvAsBytes, csv);
    }
}