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

    @BeforeEach
    void setUp() throws RecordDoesNotExistException, UserIsNotOwnerException {
        MockitoAnnotations.openMocks(this);

        User user = User.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .email("test@mail.com")
                .password("password")
                .role(Role.USER)
                .build();
        when(userService.getCurrentUser()).thenReturn(user);

        User user2 = User.builder()
                .id(UUID.fromString("77f57e8c-f7a4-4ff3-bb18-bd448b7a3019"))
                .email("test2@mail.com")
                .password("password")
                .role(Role.USER)
                .build();

        Account account = Account.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .user(user)
                .build();
        when(accountService.findCurrentUserAccount(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))).thenReturn(account);
        when(accountService.findCurrentUserAccount(UUID.fromString("77f57e8c-f7a4-4ff3-bb18-bd448b7a3019"))).thenThrow(UserIsNotOwnerException.class);
        when(accountService.findCurrentUserAccount(UUID.fromString("4f23541e-b244-4e18-a17e-620e5d6feb1a"))).thenThrow(RecordDoesNotExistException.class);
        when(modelMapper.map(account, AccountResponse.class)).thenReturn(AccountResponse.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .name("USD account")
                .currencyCode("USD")
                .build());

        Account account2 = Account.builder()
                .id(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"))
                .name("PLN account")
                .currencyCode(CurrencyCode.PLN)
                .user(user)
                .build();
        when(accountService.findCurrentUserAccount(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"))).thenReturn(account2);
        when(modelMapper.map(account2, AccountResponse.class)).thenReturn(AccountResponse.builder()
                .id(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"))
                .name("PLN account")
                .currencyCode("PLN")
                .build());

        Category category = Category.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .name("Food")
                .build();
        when(categoryService.findCurrentUserCategory("Food")).thenReturn(category);
        when(categoryService.findCurrentUserCategory("Healthcare")).thenThrow(RecordDoesNotExistException.class);

        Transaction transaction = Transaction.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .user(user)
                .amount(BigDecimal.valueOf(100, 2))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .category(category)
                .account(account)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(UUID.fromString("77f57e8c-f7a4-4ff3-bb18-bd448b7a3019"))
                .user(user2)
                .amount(BigDecimal.valueOf(100, 2))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .category(category)
                .account(account)
                .build();

        when(transactionRepository.findById(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))).thenReturn(Optional.of(transaction));
        when(transactionRepository.findById(UUID.fromString("77f57e8c-f7a4-4ff3-bb18-bd448b7a3019"))).thenReturn(Optional.of(transaction2));
        when(transactionRepository.findById(UUID.fromString("4f23541e-b244-4e18-a17e-620e5d6feb1a"))).thenReturn(Optional.empty());

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
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .amount("100.00")
                .date("2024-01-01")
                .paymentMethod("CASH")
                .category(CategoryResponse.builder()
                        .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                        .name("Food")
                        .build())
                .account(AccountResponse.builder()
                        .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
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
        when(transactionRepository.findById(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))).thenReturn(Optional.of(transaction));

        Transaction convertedTransaction = Transaction.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
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
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .amount("421.00")
                .date("2024-01-01")
                .paymentMethod("CASH")
                .category(CategoryResponse.builder()
                        .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                        .name("Food")
                        .build())
                .account(AccountResponse.builder()
                        .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                        .name("PLN account")
                        .currencyCode("PLN")
                        .build())
                .build();

        when(modelMapper.mapTransactionRequestToTransaction(convertedTransactionRequest, account2, category)).thenReturn(convertedTransaction);
        when(modelMapper.map(convertedTransaction, TransactionResponse.class)).thenReturn(convertedTransactionResponse);
        when(transactionRepository.save(convertedTransaction)).thenReturn(convertedTransaction);

        Transaction convertedTransaction_2 = Transaction.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .amount(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .category(category)
                .account(account2)
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
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .amount("421.00")
                .date("2024-01-01")
                .paymentMethod("CASH")
                .category(CategoryResponse.builder()
                        .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                        .name("Food")
                        .build())
                .account(AccountResponse.builder()
                        .id(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"))
                        .name("PLN account")
                        .currencyCode("PLN")
                        .build())
                .details("EUR->PLN: 4.21")
                .build();

        when(modelMapper.mapTransactionRequestToTransaction(convertedTransactionRequest_2, account2, category)).thenReturn(convertedTransaction_2);
        when(modelMapper.map(convertedTransaction_2, TransactionResponse.class)).thenReturn(convertedTransactionResponse_2);
        when(transactionRepository.save(convertedTransaction_2)).thenReturn(convertedTransaction_2);
        when(exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.PLN)).thenReturn(ExchangeResponse.builder().conversionRate("4.21").build());

                Transaction convertedTransaction_3 = Transaction.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .amount(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP))
                .date(LocalDate.of(2024, 1, 1))
                .paymentMethod(PaymentMethod.CASH)
                .category(category)
                .account(account2)
                .build();

        TransactionRequest convertedTransactionRequest_3 = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(421).setScale(2, RoundingMode.HALF_UP))
                .categoryName(CategoryRequest.builder().name("Food").build())
                .date(LocalDate.of(2024, 1, 1))
                .currencyCode(CurrencyCode.PLN)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        TransactionResponse convertedTransactionResponse_3 = TransactionResponse.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .amount("421.00")
                .date("2024-01-01")
                .paymentMethod("CASH")
                .category(CategoryResponse.builder()
                        .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                        .name("Food")
                        .build())
                .account(AccountResponse.builder()
                        .id(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"))
                        .name("PLN account")
                        .currencyCode("PLN")
                        .build())
                .build();

        when(modelMapper.mapTransactionRequestToTransaction(convertedTransactionRequest_3, account2, category)).thenReturn(convertedTransaction_3);
        when(modelMapper.map(convertedTransaction_3, TransactionResponse.class)).thenReturn(convertedTransactionResponse_3);
        when(transactionRepository.save(convertedTransaction_3)).thenReturn(convertedTransaction_3);
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
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"), pageable);

        assertEquals(1, returnedTransactionResponses.getTotalElements());
    }

    @Test
    void shouldReturnEmptyPageWhenPageOneIsEmpty() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var pageable = PageRequest.of(1, 10);
        var transactionResponses = transactionService.findCurrentUserTransactionsAsResponses(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"), pageable);

        assertEquals(0, transactionResponses.getTotalElements());
    }

    @Test
    void shouldThrowExceptionWhenAccountBelongToAnotherUser() {
        var pageable = PageRequest.of(0, 10);

        assertThrows(UserIsNotOwnerException.class,
                () -> transactionService.findCurrentUserTransactionsAsResponses(UUID.fromString("77f57e8c-f7a4-4ff3-bb18-bd448b7a3019"), pageable));
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() {
        var pageable = PageRequest.of(0, 10);

        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findCurrentUserTransactionsAsResponses(UUID.fromString("4f23541e-b244-4e18-a17e-620e5d6feb1a"), pageable));
    }

    @Test
    void shouldReturnPageWithTransactionResponseCategory() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var pageable = PageRequest.of(0, 10);
        var categoryRequest = CategoryRequest.builder().name("Food").build();
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses(
                UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"), categoryRequest, pageable);

        assertEquals(1, returnedTransactionResponses.getTotalElements());
    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        var pageable = PageRequest.of(0, 10);
        var categoryRequest = CategoryRequest.builder().name("Healthcare").build();

        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.findCurrentUserTransactionsAsResponses(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"), categoryRequest, pageable));
    }


    @Test
    void shouldReturnTransactionResponseDateBetween() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var pageable = PageRequest.of(0, 10);
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);
        var returnedTransactionResponses = transactionService.findCurrentUserTransactionsAsResponses(
                UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"), startDate, endDate, pageable);

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
                UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"), startDate, endDate, pageable);

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
               UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"), categoryRequest, startDate, endDate, pageable);

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

        var returnedTransactionResponse = transactionService.createTransactionForCurrentUser(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"), transactionRequest);

        var transactionResponse = TransactionResponse.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .amount("100.00")
                .date("2024-01-01")
                .paymentMethod("CASH")
                .category(CategoryResponse.builder()
                        .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                        .name("Food")
                        .build())
                .account(AccountResponse.builder()
                        .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
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
                () -> transactionService.createTransactionForCurrentUser(UUID.fromString("4f23541e-b244-4e18-a17e-620e5d6feb1a"), transactionRequest));

    }

    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist_Create() {
        var transactionRequest = TransactionRequest.builder()
                .categoryName(CategoryRequest.builder().name("Healthcare").build())
                .build();
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.createTransactionForCurrentUser(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"), transactionRequest));
    }

    @Test
    void shouldThrowExceptionWhenAccountBelongToAnotherUser_Create() {
        var transactionRequest = TransactionRequest.builder()
                .categoryName(CategoryRequest.builder().name("Food").build())
                .build();
        assertThrows(UserIsNotOwnerException.class,
                () -> transactionService.createTransactionForCurrentUser(UUID.fromString("77f57e8c-f7a4-4ff3-bb18-bd448b7a3019"), transactionRequest));
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
                    UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"), BigDecimal.valueOf(4.21), transactionRequest);

            var transactionResponse = TransactionResponse.builder()
                    .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                    .amount("421.00")
                    .date("2024-01-01")
                    .paymentMethod("CASH")
                    .category(CategoryResponse.builder()
                            .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                            .name("Food")
                            .build())
                    .account(AccountResponse.builder()
                            .id(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"))
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
                    UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"), null, transactionRequest);

            var transactionResponse = TransactionResponse.builder()
                    .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                    .amount("421.00")
                    .date("2024-01-01")
                    .paymentMethod("CASH")
                    .category(CategoryResponse.builder()
                            .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                            .name("Food")
                            .build())
                    .account(AccountResponse.builder()
                            .id(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"))
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
        assertDoesNotThrow(() -> transactionService.deleteCurrentUserTransaction(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad")));
    }

    @Test
    void shouldThrowExceptionWhenTransactionDoesNotExist() {
        assertThrows(RecordDoesNotExistException.class,
                () -> transactionService.deleteCurrentUserTransaction(UUID.fromString("4f23541e-b244-4e18-a17e-620e5d6feb1a")));
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwnerWhenDeleting() {
        assertThrows(UserIsNotOwnerException.class,
                () -> transactionService.deleteCurrentUserTransaction(UUID.fromString("77f57e8c-f7a4-4ff3-bb18-bd448b7a3019")));
    }

    @Test
    void shouldReturnSumPositiveAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserPositiveAmount(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"));

        AccountResponse account = AccountResponse.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .name("USD account")
                .currencyCode("USD")
                .build();

        assertEquals(SumResponse.builder().amount("100.10").account(account).build(), sumPositiveAmount);
    }

    @Test
    void shouldReturnSumAs0PositiveAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserPositiveAmount(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"));

        AccountResponse account = AccountResponse.builder()
                .id(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"))
                .name("PLN account")
                .currencyCode("PLN")
                .build();
        assertEquals(SumResponse.builder().amount("0.00").account(account).build(), sumPositiveAmount);

    }

    @Test
    void shouldReturnSumNegativeAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumNegativeAmount = transactionService.sumCurrentUserNegativeAmount(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"));

        AccountResponse account = AccountResponse.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .name("USD account")
                .currencyCode("USD")
                .build();
        assertEquals(SumResponse.builder().amount("100.10").account(account).build(), sumNegativeAmount);
    }

    @Test
    void shouldReturnSumAs0NegativeAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserNegativeAmount(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"));

        AccountResponse account = AccountResponse.builder()
                .id(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"))
                .name("PLN account")
                .currencyCode("PLN")
                .build();
        assertEquals(SumResponse.builder().amount("0.00").account(account).build(), sumPositiveAmount);
    }

    @Test
    void shouldReturnSumAllAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumAllAmount = transactionService.sumCurrentUserAmount(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"));

        AccountResponse account = AccountResponse.builder()
                .id(UUID.fromString("212a0e7e-24c3-4774-a46b-741d89072fad"))
                .name("USD account")
                .currencyCode("USD")
                .build();
        assertEquals(SumResponse.builder().amount("100.10").account(account).build(), sumAllAmount);
    }

    @Test
    void shouldReturnSumAs0AllAmount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var sumPositiveAmount = transactionService.sumCurrentUserAmount(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"));

        AccountResponse account = AccountResponse.builder()
                .id(UUID.fromString("43823673-fa1b-45fd-900f-374505b9a454"))
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
                "212a0e7e-24c3-4774-a46b-741d89072fad,100.00,Food,2024-01-01,USD,CASH\n";
        var expectedCsvAsBytes = expectedCsv.getBytes();

        assertArrayEquals(expectedCsvAsBytes, csv);
    }
}