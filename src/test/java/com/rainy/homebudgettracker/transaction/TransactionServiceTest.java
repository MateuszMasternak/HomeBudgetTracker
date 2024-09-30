package com.rainy.homebudgettracker.transaction;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.account.AccountService;
import com.rainy.homebudgettracker.auth.UserDetailsServiceImpl;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.category.CategoryService;
import com.rainy.homebudgettracker.exchange.ExchangeResponse;
import com.rainy.homebudgettracker.exchange.ExchangeServiceImpl;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.PaymentMethod;
import com.rainy.homebudgettracker.user.Role;
import com.rainy.homebudgettracker.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {
    private TransactionService transactionService;
    private User user;
    private Transaction transaction;
    private Transaction transaction2;
    private TransactionRequest transactionRequest;
    private TransactionRequest transactionRequest2;

    @BeforeEach
    void setUp() throws RecordDoesNotExistException {
        var transactionRepository = mock(TransactionRepository.class);
        var categoryService = mock(CategoryService.class);
        var accountService = mock(AccountService.class);
        var exchangeService = mock(ExchangeServiceImpl.class);
        var userDetailsService = mock(UserDetailsServiceImpl.class);
        var modelMapper = mock(ModelMapper.class);

        user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();
        when(userDetailsService.getCurrentUser()).thenReturn(user);

        var account = Account.builder()
                .id(1L)
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .user(user)
                .build();

        var account2 = Account.builder()
                .id(1L)
                .name("PLN account")
                .currencyCode(CurrencyCode.PLN)
                .user(user)
                .build();

        var category = Category.builder()
                .id(1L)
                .name("Food")
                .user(user)
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .account(account)
                .category(category)
                .amount(BigDecimal.valueOf(100))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .user(user)
                .build();

        transaction2 = Transaction.builder()
                .id(1L)
                .account(account2)
                .category(category)
                .amount(BigDecimal.valueOf(421).setScale(2))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .user(user)
                .build();

        transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .category(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.USD)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        transactionRequest2 = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(421.00).setScale(2))
                .category(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.PLN)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        var pageable = PageRequest.of(0, 10);
        var transactionPage = new PageImpl<>(List.of(transaction));
        var pageableEmpty = PageRequest.of(1, 10);
        var transactionPageEmpty = new PageImpl<Transaction>(List.of());

        when(modelMapper.map(transaction, TransactionResponse.class)).thenReturn(TransactionResponse.builder()
                .id(transaction.getId())
                .amount(String.valueOf(transaction.getAmount()))
                .date(String.valueOf(transaction.getDate()))
                .paymentMethod(transaction.getPaymentMethod().toString())
                .category(CategoryResponse.builder()
                        .id(transaction.getCategory().getId())
                        .name(transaction.getCategory().getName())
                        .build())
                .account(AccountResponse.builder()
                        .id(transaction.getAccount().getId())
                        .name(transaction.getAccount().getName())
                        .currencyCode(transaction.getAccount().getCurrencyCode().toString())
                        .build())
                .build());
        when(modelMapper.map(transaction2, TransactionResponse.class)).thenReturn(TransactionResponse.builder()
                .id(transaction2.getId())
                .amount(String.valueOf(transaction2.getAmount()))
                .date(String.valueOf(transaction2.getDate()))
                .paymentMethod(transaction2.getPaymentMethod().toString())
                .category(CategoryResponse.builder()
                        .id(transaction2.getCategory().getId())
                        .name(transaction2.getCategory().getName())
                        .build())
                .build());
        when(modelMapper.mapTransactionRequestToTransaction(transactionRequest, account, category)).thenReturn(transaction);
        when(modelMapper.mapTransactionRequestToTransaction(transactionRequest2, account2, category)).thenReturn(transaction2);

        when(accountService.findOneByCurrentUserAndCurrencyCode(CurrencyCode.USD)).thenReturn(account);
        when(accountService.findOneByCurrentUserAndCurrencyCode(CurrencyCode.EUR)).thenThrow(
                new RecordDoesNotExistException("Account does not exist"));
        when(accountService.findOneByCurrentUserAndCurrencyCode(CurrencyCode.PLN)).thenReturn(account2);
        when(categoryService.findOneByCurrentUserAndName("Food")).thenReturn(category);
        when(categoryService.findOneByCurrentUserAndName("Healthcare")).thenThrow(
                new RecordDoesNotExistException("Category does not exist"));
        when(transactionRepository.findAllByUserAndAccount(user, account, pageable)).thenReturn(transactionPage);
        when(transactionRepository.findAllByUserAndAccount(user, account, pageableEmpty)).thenReturn(transactionPageEmpty);
        when(transactionRepository.findAllByUserAndAccountAndCategory(user, account, category, pageable)).thenReturn(
                transactionPage);
        when(transactionRepository.findAllByUserAndAccountAndDateBetween(
                user,
                account,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                pageable))
                .thenReturn(transactionPage);
        when(transactionRepository.findAllByUserAndAccountAndDateBetween(
                user,
                account,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 2, 29),
                pageable))
                .thenReturn(transactionPageEmpty);
        when(transactionRepository.findAllByUserAndAccountAndCategoryAndDateBetween(
                user,
                account,
                category,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 31),
                pageable))
                .thenReturn(transactionPage);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionRepository.save(transaction2)).thenReturn(transaction2);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.findById(2L)).thenReturn(Optional.of(Transaction.builder()
                .id(2L)
                .account(account)
                .category(category)
                .amount(BigDecimal.valueOf(100))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .user(User.builder()
                        .id(2L)
                        .email("throwexception@mail.com")
                        .password("password")
                        .role(Role.USER)
                        .build())
                .build()));
        when(transactionRepository.findById(3L)).thenReturn(Optional.empty());
        doNothing().when(transactionRepository).deleteById(1L);
        when(transactionRepository.sumPositiveAmountByUserAndAccount(user, account)).thenReturn(BigDecimal.valueOf(100));
        when(transactionRepository.sumPositiveAmountByUserAndAccount(user, account2)).thenReturn(null);
        when(transactionRepository.sumAmountByUserAndAccount(user, account)).thenReturn(BigDecimal.valueOf(100));
        when(transactionRepository.sumAmountByUserAndAccount(user, account2)).thenReturn(null);
        when(transactionRepository.sumNegativeAmountByUserAndAccount(user, account)).thenReturn(BigDecimal.valueOf(100));
        when(transactionRepository.sumNegativeAmountByUserAndAccount(user, account2)).thenReturn(null);
        when(transactionRepository.findAllByUser(user)).thenReturn(List.of(transaction));

        when(exchangeService.getExchangeRate(CurrencyCode.USD, CurrencyCode.PLN)).thenReturn(ExchangeResponse.builder()
                .baseCode("USD")
                .targetCode("PLN")
                .conversionRate("4.21")
                .build());

        transactionService = new TransactionServiceImpl(
                transactionRepository, categoryService, accountService, exchangeService, modelMapper, userDetailsService);
    }

    @Test
    void shouldReturnPageWithTransactionResponse() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var transactionResponses = transactionService.findAllByCurrentUserAndAccount(CurrencyCode.USD, pageable);
        assertEquals(1, transactionResponses.getTotalElements());
        assertEquals(transaction.getId(), transactionResponses.getContent().get(0).getId());
    }

    @Test
    void shouldReturnEmptyPageWhenPageOneIsEmpty() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(1, 10);
        var transactionResponses = transactionService.findAllByCurrentUserAndAccount(CurrencyCode.USD, pageable);
        assertEquals(0, transactionResponses.getTotalElements());
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() {
        var pageable = PageRequest.of(0, 10);
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findAllByCurrentUserAndAccount(CurrencyCode.EUR, pageable));
    }

    @Test
    void shouldReturnPageWithTransactionResponseCategory() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var categoryRequest = CategoryRequest.builder().name("Food").build();
        var transactionResponses = transactionService.findAllByCurrentUserAndAccountAndCategory(
                CurrencyCode.USD, categoryRequest, pageable);
        var transactionResponse = transactionResponses.getContent().get(0);
        assertEquals(1, transactionResponses.getTotalElements());
        assertEquals(transaction.getId(), transactionResponse.getId());
        assertEquals(transaction.getAmount().toString(), transactionResponse.getAmount());
        assertEquals(transaction.getDate().toString(), transactionResponse.getDate());
        assertEquals(transaction.getPaymentMethod().toString(), transactionResponse.getPaymentMethod());
        assertEquals(transaction.getCategory().getName(), transactionResponse.getCategory().getName());
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        var pageable = PageRequest.of(0, 10);
        var categoryRequest = CategoryRequest.builder().name("Healthcare").build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findAllByCurrentUserAndAccountAndCategory(CurrencyCode.USD, categoryRequest, pageable));
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExistCategory() {
        var pageable = PageRequest.of(0, 10);
        var categoryRequest = CategoryRequest.builder().name("Food").build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findAllByCurrentUserAndAccountAndCategory(CurrencyCode.EUR, categoryRequest, pageable));
    }

    @Test
    void shouldReturnTransactionResponseDateBetween() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var transactionResponses = transactionService.findAllByCurrentUserAndAccountAndDateBetween(
                CurrencyCode.USD, startDate, endDate, pageable);
        var transactionResponse = transactionResponses.getContent().get(0);
        assertEquals(transaction.getId(), transactionResponse.getId());
        assertEquals(transaction.getAmount().toString(), transactionResponse.getAmount());
        assertEquals(transaction.getDate().toString(), transactionResponse.getDate());
        assertEquals(transaction.getPaymentMethod().toString(), transactionResponse.getPaymentMethod());
        assertEquals(transaction.getCategory().getName(), transactionResponse.getCategory().getName());
    }

    @Test
    void shouldReturnEmptyPageWhenPageOneIsEmptyDateBetween() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 2, 1);
        var endDate = LocalDate.of(2024, 2, 29);
        var transactionResponses = transactionService.findAllByCurrentUserAndAccountAndDateBetween(
                CurrencyCode.USD, startDate, endDate, pageable);
        assertEquals(0, transactionResponses.getTotalElements());
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExistDateBetween() {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findAllByCurrentUserAndAccountAndDateBetween(
                        CurrencyCode.EUR, startDate, endDate, pageable));
    }

    @Test
    void shouldReturnTransactionResponseCategoryDateBetween() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var transactionResponses = transactionService.findAllByCurrentUserAndAccountAndCategoryAndDateBetween(
                CurrencyCode.USD, "Food", startDate, endDate, pageable);
        var transactionResponse = transactionResponses.getContent().get(0);
        assertEquals(transaction.getId(), transactionResponse.getId());
        assertEquals(transaction.getAmount().toString(), transactionResponse.getAmount());
        assertEquals(transaction.getDate().toString(), transactionResponse.getDate());
        assertEquals(transaction.getPaymentMethod().toString(), transactionResponse.getPaymentMethod());
        assertEquals(transaction.getCategory().getName(), transactionResponse.getCategory().getName());
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExistDateBetween() {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findAllByCurrentUserAndAccountAndCategoryAndDateBetween(
                        CurrencyCode.USD, "Healthcare", startDate, endDate, pageable));
    }

    // CREATE 1
    @Test
    void shouldReturnTransactionResponseWhenTransactionIsCreated() throws RecordDoesNotExistException {
        var transactionResponse = transactionService.createTransactionForCurrentUser(transactionRequest);
        assertEquals(transaction.getId(), transactionResponse.getId());
        assertEquals(transaction.getAmount().toString(), transactionResponse.getAmount());
        assertEquals(transaction.getDate().toString(), transactionResponse.getDate());
        assertEquals(transaction.getPaymentMethod().toString(), transactionResponse.getPaymentMethod());
        assertEquals(transaction.getCategory().getName(), transactionResponse.getCategory().getName());
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExistCreate() {
        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .category(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.EUR)
                .paymentMethod(PaymentMethod.CASH)
                .build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.createTransactionForCurrentUser(transactionRequest));
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExistCreate() {
        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .category(CategoryRequest.builder().name("Healthcare").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.USD)
                .paymentMethod(PaymentMethod.CASH)
                .build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.createTransactionForCurrentUser(transactionRequest));
    }

    // CREATE 2
    @Test
    void shouldReturnTransactionResponseWhenTransactionIsCreated2() throws RecordDoesNotExistException {
        var transactionResponse = transactionService.createTransactionForCurrentUser(
                CurrencyCode.PLN, BigDecimal.valueOf(4.21), transactionRequest);
        assertEquals(transaction2.getId(), transactionResponse.getId());
        assertEquals(transaction2.getAmount().toString(), transactionResponse.getAmount());
        assertEquals(transaction.getDate().toString(), transactionResponse.getDate());
        assertEquals(transaction.getPaymentMethod().toString(), transactionResponse.getPaymentMethod());
        assertEquals(transaction.getCategory().getName(), transactionResponse.getCategory().getName());
    }

    @Test
    void shouldReturnTransactionResponseWhenTransactionIsCreated2NullExchangeRate() throws RecordDoesNotExistException {
        var transactionResponse = transactionService.createTransactionForCurrentUser(
                CurrencyCode.PLN, null, transactionRequest);
        assertEquals(transaction2.getId(), transactionResponse.getId());
        assertEquals(transaction2.getAmount().toString(), transactionResponse.getAmount());
        assertEquals(transaction.getDate().toString(), transactionResponse.getDate());
        assertEquals(transaction.getPaymentMethod().toString(), transactionResponse.getPaymentMethod());
        assertEquals(transaction.getCategory().getName(), transactionResponse.getCategory().getName());
    }

    // DELETE
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

    // SUM POSITIVE
    @Test
    void shouldReturnSumPositiveAmount() throws RecordDoesNotExistException {
        var sumPositiveAmount = transactionService.sumPositiveAmountByCurrentUserAndAccount(CurrencyCode.USD);
        assertEquals(SumResponse.builder().amount("100").build().getAmount(), sumPositiveAmount.getAmount());
    }

    @Test
    void shouldReturnSumAs0PositiveAmount() throws RecordDoesNotExistException {
        var sumPositiveAmount = transactionService.sumPositiveAmountByCurrentUserAndAccount(CurrencyCode.PLN);
        assertEquals(SumResponse.builder().amount("0").build().getAmount(), sumPositiveAmount.getAmount());
    }

    // SUM NEGATIVE
    @Test
    void shouldReturnSumNegativeAmount() throws RecordDoesNotExistException {
        var sumNegativeAmount = transactionService.sumNegativeAmountByCurrentUserAndAccount(CurrencyCode.USD);
        assertEquals(SumResponse.builder().amount("100").build().getAmount(), sumNegativeAmount.getAmount());
    }

    @Test
    void shouldReturnSumAs0NegativeAmount() throws RecordDoesNotExistException {
        var sumNegativeAmount = transactionService.sumNegativeAmountByCurrentUserAndAccount(CurrencyCode.PLN);
        assertEquals(SumResponse.builder().amount("0").build().getAmount(), sumNegativeAmount.getAmount());
    }

    // SUM ALL
    @Test
    void shouldReturnSumAllAmount() throws RecordDoesNotExistException {
        var sumAllAmount = transactionService.sumAmountByCurrentUserAndAccount(CurrencyCode.USD);
        assertEquals(SumResponse.builder().amount("100").build().getAmount(), sumAllAmount.getAmount());
    }

    @Test
    void shouldReturnSumAs0AllAmount() throws RecordDoesNotExistException {
        var sumAllAmount = transactionService.sumAmountByCurrentUserAndAccount(CurrencyCode.PLN);
        assertEquals(SumResponse.builder().amount("0").build().getAmount(), sumAllAmount.getAmount());
    }

    // FIND ALL AS LIST
    @Test
    void shouldReturnListWithTransactionResponse() throws RecordDoesNotExistException {
        var transactionResponses = transactionService.findAllByCurrentUser();
        assertEquals(1, transactionResponses.size());
        assertEquals(transaction.getId(), transactionResponses.get(0).getId());
    }

    // CSV EXPORT
    @Test
    void shouldReturnCsvString() throws IOException {
        var csv = transactionService.generateCsvFileForCurrentUserTransactions();
        var expectedCsv = "sep=,\n" +
                "ID,Amount,Category,Date,Currency code,Payment method\n" +
                "1,100,Food,2024-01-01,USD,CASH\n";
        var expectedCsvAsBytes = expectedCsv.getBytes();
        assertArrayEquals(expectedCsvAsBytes, csv);
    }

}