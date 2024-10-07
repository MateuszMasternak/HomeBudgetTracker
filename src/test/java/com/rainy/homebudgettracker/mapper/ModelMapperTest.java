package com.rainy.homebudgettracker.mapper;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountRequest;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.auth.UserDetailsServiceImpl;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.transaction.Transaction;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModelMapperTest {
    @InjectMocks
    ModelMapper modelMapper;
    @Mock
    UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        var user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();

        when(userDetailsService.getCurrentUser()).thenReturn(user);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void shouldMapCategoryToCategoryResponse() {
        var category = Category.builder()
                .id(1L)
                .name("Food")
                .user(User.builder()
                        .id(1L)
                        .email("mail@mail.com")
                        .password("password")
                        .role(Role.USER)
                        .build())
                .build();

        var returnedCategoryResponse = modelMapper.map(category, CategoryResponse.class);

        var categoryResponse = CategoryResponse.builder()
                .id(1L)
                .name("Food")
                .build();

        assertEquals(categoryResponse, returnedCategoryResponse);
    }

    @Test
    public void shouldThrowExceptionCategoryResponse() {
        var exception = assertThrows(
                UnsupportedOperationException.class, () -> modelMapper.map(new Object(), CategoryResponse.class));
        assertEquals("Mapping not supported", exception.getMessage());
    }

    @Test
    public void shouldMapCategoryRequestToCategory() {
        var categoryRequest = CategoryRequest.builder()
                .name("Food")
                .build();

        var returnedCategory = modelMapper.map(categoryRequest, Category.class);

        var category = Category.builder()
                .name("Food")
                .user(User.builder()
                        .id(1L)
                        .email("mail@mail.com")
                        .password("password")
                        .role(Role.USER)
                        .build())
                .build();

        assertEquals(category, returnedCategory);

        verify(userDetailsService, times(1)).getCurrentUser();
    }

    @Test
    public void shouldThrowExceptionCategory() {
        var exception = assertThrows(
                UnsupportedOperationException.class, () -> modelMapper.map(new Object(), CategoryRequest.class));
        assertEquals("Mapping not supported", exception.getMessage());

        verify(userDetailsService, times(0)).getCurrentUser();
    }

    @Test
    public void shouldMapAccountToAccountResponse() {
        var account = Account.builder()
                .id(1L)
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .user(User.builder()
                        .id(1L)
                        .email("mail@mail.com")
                        .password("password")
                        .role(Role.USER)
                        .build())
                .build();

        var returnedAccountResponse = modelMapper.map(account, AccountResponse.class);

        var accountResponse = AccountResponse.builder()
                .id(1L)
                .name("Main")
                .currencyCode(CurrencyCode.USD.name())
                .build();

        assertEquals(accountResponse, returnedAccountResponse);
    }

    @Test
    public void shouldThrowExceptionAccountResponse() {
        var exception = assertThrows(
                UnsupportedOperationException.class, () -> modelMapper.map(new Object(), AccountResponse.class));
        assertEquals("Mapping not supported", exception.getMessage());
    }

    @Test
    public void shouldMapAccountRequestToAccount() {
        var accountRequest = com.rainy.homebudgettracker.account.AccountRequest.builder()
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .build();

        var returnedAccount = modelMapper.map(accountRequest, Account.class);

        var account = Account.builder()
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .user(User.builder()
                        .id(1L)
                        .email("mail@mail.com")
                        .password("password")
                        .role(Role.USER)
                        .build())
                .build();

        assertEquals(account, returnedAccount);

        verify(userDetailsService, times(1)).getCurrentUser();
    }

    @Test
    public void shouldThrowExceptionAccount() {
        var exception = assertThrows(
                UnsupportedOperationException.class, () -> modelMapper.map(new Object(), AccountRequest.class));
        assertEquals("Mapping not supported", exception.getMessage());

        verify(userDetailsService, times(0)).getCurrentUser();
    }

    @Test
    public void TransactionRequestToTransaction() {
        var user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();

        var account = Account.builder()
                .id(1L)
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .user(user)
                .build();
        var category = Category.builder()
                .id(1L)
                .name("Food")
                .user(user)
                .build();

        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .date(LocalDate.now())
                .paymentMethod(PaymentMethod.CASH)
                .category(CategoryRequest.builder().name("Food").build())
                .currencyCode(CurrencyCode.USD)
                .build();

        var returnedTransaction = modelMapper.mapTransactionRequestToTransaction(transactionRequest, account, category);

        var transaction = Transaction.builder()
                .amount(transactionRequest.getAmount())
                .category(category)
                .date(transactionRequest.getDate())
                .account(account)
                .paymentMethod(transactionRequest.getPaymentMethod())
                .user(user)
                .build();

        assertEquals(transaction, returnedTransaction);
    }

    @Test
    public void shouldThrowExceptionTransaction() {
        var exception = assertThrows(
                UnsupportedOperationException.class, () -> modelMapper.map(new Object(), TransactionRequest.class));
        assertEquals("Mapping not supported", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionDefault() {
        var exception = assertThrows(
                UnsupportedOperationException.class, () -> modelMapper.map(new Object(), Object.class));
        assertEquals("Mapping not supported", exception.getMessage());
    }

    @Test
    public void shouldMapTransactionRequestToTransaction() {
        var user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();

        var account = Account.builder()
                .id(1L)
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .user(user)
                .build();
        var category = Category.builder()
                .id(1L)
                .name("Food")
                .user(user)
                .build();

        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .date(LocalDate.now())
                .paymentMethod(PaymentMethod.CASH)
                .category(CategoryRequest.builder().name("Food").build())
                .currencyCode(CurrencyCode.USD)
                .build();

        var returnedTransaction = modelMapper.mapTransactionRequestToTransaction(transactionRequest, account, category);

        var transaction = Transaction.builder()
                .amount(transactionRequest.getAmount())
                .category(category)
                .date(transactionRequest.getDate())
                .account(account)
                .paymentMethod(transactionRequest.getPaymentMethod())
                .user(user)
                .build();

        assertEquals(transaction, returnedTransaction);
    }
}