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
import com.rainy.homebudgettracker.images.ImageService;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.TransactionMethod;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @Mock
    ImageService imageService;

    @BeforeEach
    void setUp() throws RecordDoesNotExistException, UserIsNotOwnerException {
        MockitoAnnotations.openMocks(this);
        when(userService.getUserSub()).thenReturn(TestData.USER_SUB);

        when(imageService.getImageUrl(any())).thenReturn(TestData.IMAGE_URL);

        when(accountService.findCurrentUserAccount(UUID.fromString(TestData.ACCOUNT_ID))).thenReturn(TestData.ACCOUNT);
        when(accountService.findCurrentUserAccount(UUID.fromString(TestData.OTHER_USER_ACCOUNT_ID))).thenThrow(UserIsNotOwnerException.class);
        when(accountService.findCurrentUserAccount(UUID.fromString(TestData.NON_EXISTENT_ACCOUNT_ID))).thenThrow(RecordDoesNotExistException.class);
        when(modelMapper.map(TestData.ACCOUNT, AccountResponse.class)).thenReturn(TestData.ACCOUNT_RESPONSE);

        when(accountService.findCurrentUserAccount(UUID.fromString(TestData.ACCOUNT_ID_2))).thenReturn(TestData.ACCOUNT_2);
        when(modelMapper.map(TestData.ACCOUNT_2, AccountResponse.class)).thenReturn(TestData.ACCOUNT_RESPONSE_2);

        when(categoryService.findCurrentUserCategory("Food")).thenReturn(TestData.CATEGORY);
        when(categoryService.findCurrentUserCategory("Healthcare")).thenThrow(RecordDoesNotExistException.class);

        when(transactionRepository.findById(UUID.fromString(TestData.TRANSACTION_ID))).thenReturn(Optional.of(TestData.TRANSACTION));
        when(transactionRepository.findById(UUID.fromString(TestData.TRANSACTION_ID_2))).thenReturn(Optional.of(TestData.TRANSACTION_2));
        when(transactionRepository.findById(UUID.fromString(TestData.TRANSACTION_ID_3))).thenReturn(Optional.empty());

        when(transactionRepository.findAllByAccount(TestData.ACCOUNT, TestData.PAGEABLE)).thenReturn(new PageImpl<>(List.of(TestData.TRANSACTION)));
        when(transactionRepository.findAllByAccountAndCategory(TestData.ACCOUNT, TestData.CATEGORY, TestData.PAGEABLE)).thenReturn(new PageImpl<>(List.of(TestData.TRANSACTION)));
        when(transactionRepository.findAllByAccountAndDateBetween(TestData.ACCOUNT, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), TestData.PAGEABLE)).thenReturn(new PageImpl<>(List.of(TestData.TRANSACTION)));
        when(transactionRepository.findAllByAccountAndCategoryAndDateBetween(TestData.ACCOUNT, TestData.CATEGORY, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), TestData.PAGEABLE)).thenReturn(new PageImpl<>(List.of(TestData.TRANSACTION)));

        when(transactionRepository.findAllByAccount(TestData.ACCOUNT, TestData.PAGEABLE_2)).thenReturn(Page.empty());
        when(transactionRepository.findAllByAccountAndCategory(TestData.ACCOUNT, TestData.CATEGORY, TestData.PAGEABLE_2)).thenReturn(Page.empty());
        when(transactionRepository.findAllByAccountAndDateBetween(TestData.ACCOUNT, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), TestData.PAGEABLE_2)).thenReturn(Page.empty());
        when(transactionRepository.findAllByAccountAndCategoryAndDateBetween(TestData.ACCOUNT, TestData.CATEGORY, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), TestData.PAGEABLE_2)).thenReturn(Page.empty());

        when(modelMapper.map(TestData.TRANSACTION, TransactionResponse.class)).thenReturn(TestData.TRANSACTION_RESPONSE);

        when(modelMapper.map(TestData.TRANSACTION_REQUEST, Transaction.class, TestData.USER_SUB, TestData.CATEGORY, TestData.ACCOUNT)).thenReturn(TestData.TRANSACTION);

        when(transactionRepository.save(TestData.TRANSACTION)).thenReturn(TestData.TRANSACTION);
        when(transactionRepository.findById(UUID.fromString(TestData.TRANSACTION_ID))).thenReturn(Optional.of(TestData.TRANSACTION));

        when(modelMapper.map(TestData.CONVERTED_TRANSACTION_REQUEST, Transaction.class, TestData.USER_SUB, TestData.CATEGORY, TestData.ACCOUNT_2)).thenReturn(TestData.CONVERTED_TRANSACTION);
        when(modelMapper.map(TestData.CONVERTED_TRANSACTION, TransactionResponse.class)).thenReturn(TestData.CONVERTED_TRANSACTION_RESPONSE);
        when(transactionRepository.save(TestData.CONVERTED_TRANSACTION)).thenReturn(TestData.CONVERTED_TRANSACTION);

        when(modelMapper.map(TestData.CONVERTED_TRANSACTION_REQUEST_2, Transaction.class, TestData.USER_SUB, TestData.CATEGORY, TestData.ACCOUNT_2)).thenReturn(TestData.CONVERTED_TRANSACTION_2);
        when(modelMapper.map(TestData.CONVERTED_TRANSACTION_2, TransactionResponse.class)).thenReturn(TestData.CONVERTED_TRANSACTION_RESPONSE_2);
        when(transactionRepository.save(TestData.CONVERTED_TRANSACTION_2)).thenReturn(TestData.CONVERTED_TRANSACTION_2);

        when(modelMapper.map(TestData.CONVERTED_TRANSACTION_REQUEST_3, Transaction.class, TestData.USER_SUB, TestData.CATEGORY, TestData.ACCOUNT_2)).thenReturn(TestData.CONVERTED_TRANSACTION_3);
        when(modelMapper.map(TestData.CONVERTED_TRANSACTION_3, TransactionResponse.class)).thenReturn(TestData.CONVERTED_TRANSACTION_RESPONSE_3);
        when(transactionRepository.save(TestData.CONVERTED_TRANSACTION_3)).thenReturn(TestData.CONVERTED_TRANSACTION_3);
        when(exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.PLN)).thenReturn(ExchangeResponse.builder().conversionRate("4.22").build());

        when(transactionRepository.sumPositiveAmountByAccount(TestData.ACCOUNT)).thenReturn(BigDecimal.valueOf(100.1));
        when(transactionRepository.sumPositiveAmountByAccount(TestData.ACCOUNT_2)).thenReturn(BigDecimal.valueOf(0));
        when(transactionRepository.sumNegativeAmountByAccount(TestData.ACCOUNT)).thenReturn(BigDecimal.valueOf(100.1));
        when(transactionRepository.sumNegativeAmountByAccount(TestData.ACCOUNT_2)).thenReturn(BigDecimal.valueOf(0));
        when(transactionRepository.sumAmountByAccount(TestData.ACCOUNT)).thenReturn(BigDecimal.valueOf(100.1));
        when(transactionRepository.sumAmountByAccount(TestData.ACCOUNT_2)).thenReturn(BigDecimal.valueOf(0));

        when(modelMapper.map(BigDecimal.valueOf(100.1).setScale(2, RoundingMode.HALF_UP), SumResponse.class)).thenReturn(TestData.SUM_RESPONSE);
        when(modelMapper.map(BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), SumResponse.class)).thenReturn(TestData.SUM_RESPONSE_2);

        when(transactionRepository.findAllByUserSub(TestData.USER_SUB)).thenReturn(List.of(TestData.TRANSACTION));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnPageWithTransactionResponse() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var pageable = TestData.PAGEABLE;
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses(UUID.fromString(TestData.ACCOUNT_ID), pageable);

        assertEquals(1, returnedTransactionResponses.getTotalElements());
    }

    @Test
    void shouldReturnEmptyPageWhenPageOneIsEmpty() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var pageable = TestData.PAGEABLE_2;
        var transactionResponses = transactionService.findCurrentUserTransactionsAsResponses(UUID.fromString(TestData.ACCOUNT_ID), pageable);

        assertEquals(0, transactionResponses.getTotalElements());
    }

    @Test
    void shouldThrowExceptionWhenAccountBelongToAnotherUser() {
        var pageable = TestData.PAGEABLE;

        assertThrows(UserIsNotOwnerException.class,
                () -> transactionService.findCurrentUserTransactionsAsResponses(UUID.fromString(TestData.OTHER_USER_ACCOUNT_ID), pageable));
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() {
        var pageable = TestData.PAGEABLE;

        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findCurrentUserTransactionsAsResponses(UUID.fromString(TestData.NON_EXISTENT_ACCOUNT_ID), pageable));
    }

    @Test
    void shouldReturnPageWithTransactionResponseCategory() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var pageable = TestData.PAGEABLE;
        var categoryRequest = TestData.CATEGORY_REQUEST;
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses(
                UUID.fromString(TestData.ACCOUNT_ID), categoryRequest, pageable);

        assertEquals(1, returnedTransactionResponses.getTotalElements());
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        var pageable = PageRequest.of(0, 10);
        var categoryRequest = CategoryRequest.builder().name("Healthcare").build();

        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findCurrentUserTransactionsAsResponses(UUID.fromString(TestData.ACCOUNT_ID), categoryRequest, pageable));
    }


    @Test
    void shouldReturnTransactionResponseDateBetween() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var pageable = TestData.PAGEABLE;
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses(
                UUID.fromString(TestData.ACCOUNT_ID), startDate, endDate, pageable);

        assertEquals(1, returnedTransactionResponses.getTotalElements());
    }

    @Test
    void shouldReturnEmptyPageWhenPageOneIsEmptyDateBetween()
            throws RecordDoesNotExistException,
            UserIsNotOwnerException {

        var pageable = TestData.PAGEABLE_2;
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var transactionResponses = transactionService.findCurrentUserTransactionsAsResponses(
                UUID.fromString(TestData.ACCOUNT_ID), startDate, endDate, pageable);

        assertEquals(0, transactionResponses.getTotalElements());
    }

    @Test
    void shouldReturnTransactionResponseCategoryDateBetween()
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        var pageable = TestData.PAGEABLE;
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var categoryRequest = TestData.CATEGORY_REQUEST;
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses(
               UUID.fromString(TestData.ACCOUNT_ID), categoryRequest, startDate, endDate, pageable);

        assertEquals(1, returnedTransactionResponses.getTotalElements());
    }

    @Test
    void shouldReturnTransactionResponseWhenTransactionIsCreated()
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        var transactionRequest = TestData.TRANSACTION_REQUEST;

        var returnedTransactionResponse = transactionService.createTransactionForCurrentUser(UUID.fromString(TestData.ACCOUNT_ID), transactionRequest);

        var transactionResponse = TestData.TRANSACTION_RESPONSE;

        assertEquals(transactionResponse, returnedTransactionResponse);
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist_Create() {

        var transactionRequest = TransactionRequest.builder()
                .categoryName(TestData.CATEGORY_REQUEST)
                .build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.createTransactionForCurrentUser(UUID.fromString(TestData.NON_EXISTENT_ACCOUNT_ID), transactionRequest));

    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist_Create() {
        var transactionRequest = TransactionRequest.builder()
                .categoryName(CategoryRequest.builder().name("Healthcare").build())
                .build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.createTransactionForCurrentUser(UUID.fromString(TestData.ACCOUNT_ID), transactionRequest));
    }

    @Test
    void shouldThrowExceptionWhenAccountBelongToAnotherUser_Create() {
        var transactionRequest = TransactionRequest.builder()
                .categoryName(CategoryRequest.builder().name("Food").build())
                .build();
        assertThrows(UserIsNotOwnerException.class,
                () -> transactionService.createTransactionForCurrentUser(UUID.fromString(TestData.OTHER_USER_ACCOUNT_ID), transactionRequest));
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
                .categoryName(TestData.CATEGORY_REQUEST)
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.EUR)
                .transactionMethod(TransactionMethod.CASH)
                    .build();

            var returnedTransactionResponse = transactionService.createTransactionForCurrentUser(
                    UUID.fromString(TestData.ACCOUNT_ID_2), BigDecimal.valueOf(4.21), transactionRequest);

            var transactionResponse = TestData.CONVERTED_TRANSACTION_RESPONSE;

            assertEquals(transactionResponse, returnedTransactionResponse);
        }
    }

    @Test
    void shouldReturnTransactionResponseWhenTransactionIsCreated2NullExchangeRate()
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        try(MockedStatic<CurrencyConverter> converter = Mockito.mockStatic(CurrencyConverter.class)) {
            converter.when(() -> CurrencyConverter.convert(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP),
                            BigDecimal.valueOf(4.22).setScale(2, RoundingMode.HALF_UP),
                            2))
                    .thenReturn(BigDecimal.valueOf(422).setScale(2, RoundingMode.HALF_UP));

            var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP))
                .categoryName(TestData.CATEGORY_REQUEST)
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.EUR)
                .transactionMethod(TransactionMethod.CASH)
                    .build();

            var returnedTransactionResponse = transactionService.createTransactionForCurrentUser(
                    UUID.fromString(TestData.ACCOUNT_ID_2), null, transactionRequest);

            var transactionResponse = TestData.CONVERTED_TRANSACTION_RESPONSE_3;

            assertEquals(transactionResponse, returnedTransactionResponse);
        }
    }

    @Test
    void shouldDeleteTransaction() {
        assertDoesNotThrow(() -> transactionService.deleteCurrentUserTransaction(UUID.fromString(TestData.TRANSACTION_ID)));
    }

    @Test
    void shouldThrowExceptionWhenTransactionDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.deleteCurrentUserTransaction(UUID.fromString(TestData.TRANSACTION_ID_3)));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwnerWhenDeleting() {
        assertThrows(UserIsNotOwnerException.class,
                () -> transactionService.deleteCurrentUserTransaction(UUID.fromString(TestData.TRANSACTION_ID_2)));
    }

    @Test
    void shouldReturnSumPositiveAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserPositiveAmount(UUID.fromString(TestData.ACCOUNT_ID));

        assertEquals(TestData.SUM_RESPONSE, sumPositiveAmount);
    }

    @Test
    void shouldReturnSumAs0PositiveAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserPositiveAmount(UUID.fromString(TestData.ACCOUNT_ID_2));

        assertEquals(TestData.SUM_RESPONSE_2, sumPositiveAmount);

    }

    @Test
    void shouldReturnSumNegativeAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumNegativeAmount = transactionService.sumCurrentUserNegativeAmount(UUID.fromString(TestData.ACCOUNT_ID));

        assertEquals(TestData.SUM_RESPONSE, sumNegativeAmount);
    }

    @Test
    void shouldReturnSumAs0NegativeAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserNegativeAmount(UUID.fromString(TestData.ACCOUNT_ID_2));

        assertEquals(TestData.SUM_RESPONSE_2, sumPositiveAmount);
    }

    @Test
    void shouldReturnSumAllAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumAllAmount = transactionService.sumCurrentUserAmount(UUID.fromString(TestData.ACCOUNT_ID));

        assertEquals(TestData.SUM_RESPONSE, sumAllAmount);
    }

    @Test
    void shouldReturnSumAs0AllAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserAmount(UUID.fromString(TestData.ACCOUNT_ID_2));

        assertEquals(TestData.SUM_RESPONSE_2, sumPositiveAmount);
    }

    @Test
    void shouldReturnListWithTransactionResponse() {
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses();

        assertEquals(1, returnedTransactionResponses.size());
    }
    
    @Test
    void shouldReturnCsvString() throws IOException {
        var csv = transactionService.generateCSVWithCurrentUserTransactions();
        var expectedCsv = "sep=,\n" +
                "Account name,Currency code,Amount,Category,Date,Transaction method,Description\n" +
                "USD account,USD,100.00,Food,2024-01-01,CASH,%s\n".formatted("");
        var expectedCsvAsBytes = expectedCsv.getBytes();

        assertArrayEquals(expectedCsvAsBytes, csv);
    }
}