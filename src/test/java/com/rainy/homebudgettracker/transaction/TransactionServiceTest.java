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
import com.rainy.homebudgettracker.exchange.ExchangeService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.PaymentMethod;
import com.rainy.homebudgettracker.user.Role;
import com.rainy.homebudgettracker.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {
    @InjectMocks
    private TransactionServiceImpl transactionService;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private AccountService accountService;
    @Mock
    private ExchangeService exchangeService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() throws RecordDoesNotExistException {
        MockitoAnnotations.openMocks(this);

        var user = User.builder()
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

        var transaction = Transaction.builder()
                .id(1L)
                .account(account)
                .category(category)
                .amount(BigDecimal.valueOf(100))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .user(user)
                .build();

        var transaction2 = Transaction.builder()
                .id(1L)
                .account(account2)
                .category(category)
                .amount(BigDecimal.valueOf(421).setScale(2))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .user(user)
                .build();

        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .category(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.USD)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        var transactionRequest2 = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(421).setScale(2))
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
                .account(AccountResponse.builder()
                        .id(transaction2.getAccount().getId())
                        .name(transaction2.getAccount().getName())
                        .currencyCode(transaction2.getAccount().getCurrencyCode().toString())
                        .build())
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
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnPageWithTransactionResponse() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var returnedTransactionResponses = transactionService.findAllByCurrentUserAndAccount(CurrencyCode.USD, pageable);

        var returnedTransactionResponse = returnedTransactionResponses.getContent().get(0);

        var transactionResponse = TransactionResponse.builder()
                .id(1L)
                .amount("100")
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

        assertEquals(1, returnedTransactionResponses.getTotalElements());
        assertEquals(transactionResponse, returnedTransactionResponse);

        verifyFindAllByCurrentUserAndAccount(new int[]{1, 1, 1, 1});
    }

    @Test
    void shouldReturnEmptyPageWhenPageOneIsEmpty() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(1, 10);
        var transactionResponses = transactionService.findAllByCurrentUserAndAccount(CurrencyCode.USD, pageable);

        assertEquals(0, transactionResponses.getTotalElements());

        verifyFindAllByCurrentUserAndAccount(new int[]{1, 1, 1, 0});
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findAllByCurrentUserAndAccount(CurrencyCode.EUR, pageable));

        verifyFindAllByCurrentUserAndAccount(new int[]{1, 1, 0, 0});
    }

    @Test
    void shouldReturnPageWithTransactionResponseCategory() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var categoryRequest = CategoryRequest.builder().name("Food").build();
        var returnedTransactionResponses = transactionService.findAllByCurrentUserAndAccountAndCategory(
                CurrencyCode.USD, categoryRequest, pageable);
        var returnedTransactionResponse = returnedTransactionResponses.getContent().get(0);

        var transactionResponse = TransactionResponse.builder()
                .id(1L)
                .amount("100")
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

        assertEquals(1, returnedTransactionResponses.getTotalElements());
        assertEquals(transactionResponse, returnedTransactionResponse);

        verifyFindAllByCurrentUserAndAccountAndCategory(new int[]{1, 1, 1, 1, 1});
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var categoryRequest = CategoryRequest.builder().name("Healthcare").build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findAllByCurrentUserAndAccountAndCategory(CurrencyCode.USD, categoryRequest, pageable));

        verifyFindAllByCurrentUserAndAccountAndCategory(new int[]{1, 1, 0, 0, 0});
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExistCategory() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var categoryRequest = CategoryRequest.builder().name("Food").build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findAllByCurrentUserAndAccountAndCategory(CurrencyCode.EUR, categoryRequest, pageable));

        verifyFindAllByCurrentUserAndAccountAndCategory(new int[]{1, 1, 1, 0, 0});
    }

    @Test
    void shouldReturnTransactionResponseDateBetween() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var returnedTransactionResponses = transactionService.findAllByCurrentUserAndAccountAndDateBetween(
                CurrencyCode.USD, startDate, endDate, pageable);
        var returnedTransactionResponse = returnedTransactionResponses.getContent().get(0);

        var transactionResponse = TransactionResponse.builder()
                .id(1L)
                .amount("100")
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

        assertEquals(1, returnedTransactionResponses.getTotalElements());
        assertEquals(transactionResponse, returnedTransactionResponse);

        verifyFindAllByCurrentUserAndAccountAndDateBetween(new int[]{1, 1, 1, 1});
    }

    @Test
    void shouldReturnEmptyPageWhenPageOneIsEmptyDateBetween() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 2, 1);
        var endDate = LocalDate.of(2024, 2, 29);
        var transactionResponses = transactionService.findAllByCurrentUserAndAccountAndDateBetween(
                CurrencyCode.USD, startDate, endDate, pageable);

        assertEquals(0, transactionResponses.getTotalElements());

        verifyFindAllByCurrentUserAndAccountAndDateBetween(new int[]{1, 1, 1, 0});
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExistDateBetween() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findAllByCurrentUserAndAccountAndDateBetween(
                        CurrencyCode.EUR, startDate, endDate, pageable));

        verifyFindAllByCurrentUserAndAccountAndDateBetween(new int[]{1, 1, 0, 0});
    }

    @Test
    void shouldReturnTransactionResponseCategoryDateBetween() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var returnedTransactionResponses = transactionService.findAllByCurrentUserAndAccountAndCategoryAndDateBetween(
                CurrencyCode.USD, "Food", startDate, endDate, pageable);
        var returnedTransactionResponse = returnedTransactionResponses.getContent().get(0);

        var transactionResponse = TransactionResponse.builder()
                .id(1L)
                .amount("100")
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

        assertEquals(1, returnedTransactionResponses.getTotalElements());
        assertEquals(transactionResponse, returnedTransactionResponse);

        verifyFindAllByCurrentUserAndAccountAndCategoryAndDateBetween(new int[]{1, 1, 1, 1, 1});
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExistDateBetween() throws RecordDoesNotExistException {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findAllByCurrentUserAndAccountAndCategoryAndDateBetween(
                        CurrencyCode.USD, "Healthcare", startDate, endDate, pageable));

        verifyFindAllByCurrentUserAndAccountAndCategoryAndDateBetween(new int[]{1, 1, 0, 0, 0});
    }

    // CREATE 1
    @Test
    void shouldReturnTransactionResponseWhenTransactionIsCreated() throws RecordDoesNotExistException {
        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .category(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.USD)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        var returnedTransactionResponse = transactionService.createTransactionForCurrentUser(transactionRequest);

        var transactionResponse = TransactionResponse.builder()
                .id(1L)
                .amount("100")
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

        verifyDefaultCreate(new int[]{1, 1, 1, 1, 1});
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExistCreate() throws RecordDoesNotExistException {
        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .category(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.EUR)
                .paymentMethod(PaymentMethod.CASH)
                .build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.createTransactionForCurrentUser(transactionRequest));

        verifyDefaultCreate(new int[]{1, 1, 0, 0, 0});
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExistCreate() throws RecordDoesNotExistException {
        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .category(CategoryRequest.builder().name("Healthcare").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.USD)
                .paymentMethod(PaymentMethod.CASH)
                .build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.createTransactionForCurrentUser(transactionRequest));

        verifyDefaultCreate(new int[]{1, 0, 0, 0, 0});
    }

    // CREATE 2
    @Test
    void shouldReturnTransactionResponseWhenTransactionIsCreated2() throws RecordDoesNotExistException {
        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .category(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.USD)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        var returnedTransactionResponse = transactionService.createTransactionForCurrentUser(
                CurrencyCode.PLN, BigDecimal.valueOf(4.21), transactionRequest);

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

        verifyCreateWithExchangeRate(new int[]{0, 1, 1, 1, 1, 1});
    }

    @Test
    void shouldReturnTransactionResponseWhenTransactionIsCreated2NullExchangeRate() throws RecordDoesNotExistException {
        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .category(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.USD)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        var returnedTransactionResponse = transactionService.createTransactionForCurrentUser(
                CurrencyCode.PLN, null, transactionRequest);

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

        verifyCreateWithExchangeRate(new int[]{1, 1, 1, 1, 1, 1});
    }

    // DELETE
    @Test
    void shouldDeleteTransaction() {
        assertDoesNotThrow(() -> transactionService.deleteCurrentUserTransaction(1L));

        verifyDelete(new int[]{1, 1, 1});
    }

    @Test
    void shouldThrowExceptionWhenTransactionDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.deleteCurrentUserTransaction(3L));

        verifyDelete(new int[]{1, 1, 0});
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwnerWhenDeleting() {
        assertThrows(UserIsNotOwnerException.class,
                () -> transactionService.deleteCurrentUserTransaction(2L));

        verifyDelete(new int[]{1, 1, 0});
    }

    // SUM POSITIVE
    @Test
    void shouldReturnSumPositiveAmount() throws RecordDoesNotExistException {
        var sumPositiveAmount = transactionService.sumPositiveAmountByCurrentUserAndAccount(CurrencyCode.USD);
        assertEquals(SumResponse.builder().amount("100").build().getAmount(), sumPositiveAmount.getAmount());

        verifyGetCurrentUserAndFindOneAccount(new int[]{1, 1});
        verify(transactionRepository, times(1)).sumPositiveAmountByUserAndAccount(any(User.class), any(Account.class));
    }

    @Test
    void shouldReturnSumAs0PositiveAmount() throws RecordDoesNotExistException {
        var sumPositiveAmount = transactionService.sumPositiveAmountByCurrentUserAndAccount(CurrencyCode.PLN);
        assertEquals(SumResponse.builder().amount("0").build().getAmount(), sumPositiveAmount.getAmount());

        verifyGetCurrentUserAndFindOneAccount(new int[]{1, 1});
        verify(transactionRepository, times(1)).sumPositiveAmountByUserAndAccount(any(User.class), any(Account.class));
    }

    // SUM NEGATIVE
    @Test
    void shouldReturnSumNegativeAmount() throws RecordDoesNotExistException {
        var sumNegativeAmount = transactionService.sumNegativeAmountByCurrentUserAndAccount(CurrencyCode.USD);
        assertEquals(SumResponse.builder().amount("100").build().getAmount(), sumNegativeAmount.getAmount());

        verifyGetCurrentUserAndFindOneAccount(new int[]{1, 1});
        verify(transactionRepository, times(1)).sumNegativeAmountByUserAndAccount(any(User.class), any(Account.class));
    }

    @Test
    void shouldReturnSumAs0NegativeAmount() throws RecordDoesNotExistException {
        var sumNegativeAmount = transactionService.sumNegativeAmountByCurrentUserAndAccount(CurrencyCode.PLN);
        assertEquals(SumResponse.builder().amount("0").build().getAmount(), sumNegativeAmount.getAmount());

        verifyGetCurrentUserAndFindOneAccount(new int[]{1, 1});
        verify(transactionRepository, times(1)).sumNegativeAmountByUserAndAccount(any(User.class), any(Account.class));
    }

    // SUM ALL
    @Test
    void shouldReturnSumAllAmount() throws RecordDoesNotExistException {
        var sumAllAmount = transactionService.sumAmountByCurrentUserAndAccount(CurrencyCode.USD);
        assertEquals(SumResponse.builder().amount("100").build().getAmount(), sumAllAmount.getAmount());

        verifyGetCurrentUserAndFindOneAccount(new int[]{1, 1});
        verify(transactionRepository, times(1)).sumAmountByUserAndAccount(any(User.class), any(Account.class));
    }

    @Test
    void shouldReturnSumAs0AllAmount() throws RecordDoesNotExistException {
        var sumAllAmount = transactionService.sumAmountByCurrentUserAndAccount(CurrencyCode.PLN);
        assertEquals(SumResponse.builder().amount("0").build().getAmount(), sumAllAmount.getAmount());

        verifyGetCurrentUserAndFindOneAccount(new int[]{1, 1});
        verify(transactionRepository, times(1)).sumAmountByUserAndAccount(any(User.class), any(Account.class));
    }

    // FIND ALL AS LIST
    @Test
    void shouldReturnListWithTransactionResponse() {
        var user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();

        var returnedTransactionResponses = transactionService.findAllByUser(user);
        var returnedTransactionResponse = returnedTransactionResponses.get(0);

        var transactionResponse = TransactionResponse.builder()
                .id(1L)
                .amount("100")
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

        assertEquals(1, returnedTransactionResponses.size());
        assertEquals(transactionResponse, returnedTransactionResponse);

        verify(transactionRepository, times(1)).findAllByUser(any(User.class));
        verify(modelMapper, times(1)).map(any(Transaction.class), eq(TransactionResponse.class));
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

        verify(userDetailsService, times(1)).getCurrentUser();
    }

    void verifyFindAllByCurrentUserAndAccount(int[] times) throws RecordDoesNotExistException {
        verifyGetCurrentUserAndFindOneAccountAndMap(new int[]{times[0], times[1], times[3]});
        verify(transactionRepository, times(times[2])).findAllByUserAndAccount(any(User.class), any(Account.class), any(Pageable.class));
    }

    void verifyFindAllByCurrentUserAndAccountAndCategory(int[] times) throws RecordDoesNotExistException {
        verifyGetCurrentUserAndFindOneAccountAndMap(new int[]{times[0], times[2], times[4]});
        verify(categoryService, times(times[1])).findOneByCurrentUserAndName(any(String.class));
        verify(transactionRepository, times(times[3])).findAllByUserAndAccountAndCategory(any(User.class), any(Account.class), any(Category.class), any(Pageable.class));
    }

    void verifyFindAllByCurrentUserAndAccountAndDateBetween(int[] times) throws RecordDoesNotExistException {
        verifyGetCurrentUserAndFindOneAccountAndMap(new int[]{times[0], times[1], times[3]});
        verify(transactionRepository, times(times[2])).findAllByUserAndAccountAndDateBetween(any(User.class), any(Account.class), any(LocalDate.class), any(LocalDate.class), any(Pageable.class));
    }

    void verifyFindAllByCurrentUserAndAccountAndCategoryAndDateBetween(int[] times) throws RecordDoesNotExistException {
        verifyGetCurrentUserAndFindOneAccountAndMap(new int[]{times[0], times[2], times[4]});
        verify(categoryService, times(times[1])).findOneByCurrentUserAndName(any(String.class));
        verify(transactionRepository, times(times[3])).findAllByUserAndAccountAndCategoryAndDateBetween(any(User.class), any(Account.class), any(Category.class), any(LocalDate.class), any(LocalDate.class), any(Pageable.class));
    }

    void verifyGetCurrentUserAndFindOneAccountAndMap(int[] times) throws RecordDoesNotExistException {
        verifyGetCurrentUserAndFindOneAccount(new int[]{times[0], times[1]});
        verify(modelMapper, times(times[2])).map(any(Transaction.class), eq(TransactionResponse.class));
    }

    void verifyDefaultCreate(int[] times) throws RecordDoesNotExistException {
        verify(categoryService, times(times[0])).findOneByCurrentUserAndName(any(String.class));
        verify(accountService, times(times[1])).findOneByCurrentUserAndCurrencyCode(any(CurrencyCode.class));
        verify(modelMapper, times(times[2])).mapTransactionRequestToTransaction(any(TransactionRequest.class), any(Account.class), any(Category.class));
        verify(transactionRepository, times(times[3])).save(any(Transaction.class));
        verify(modelMapper, times(times[4])).map(any(Transaction.class), eq(TransactionResponse.class));
    }

    void verifyGetCurrentUserAndFindOneAccount(int[] times) throws RecordDoesNotExistException {
        verify(userDetailsService, times(times[0])).getCurrentUser();
        verify(accountService, times(times[1])).findOneByCurrentUserAndCurrencyCode(any(CurrencyCode.class));
    }

    void verifyCreateWithExchangeRate(int[] times) throws RecordDoesNotExistException {
        verify(exchangeService, times(times[0])).getExchangeRate(any(CurrencyCode.class), any(CurrencyCode.class));
        verifyDefaultCreate(new int[]{times[1], times[2], times[3], times[4], times[5]});
    }

    void verifyDelete(int[] times) {
        verify(userDetailsService, times(times[0])).getCurrentUser();
        verify(transactionRepository, times(times[1])).findById(any(Long.class));
        verify(transactionRepository, times(times[2])).deleteById(any(Long.class));
    }
}