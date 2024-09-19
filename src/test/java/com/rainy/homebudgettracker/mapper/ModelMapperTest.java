package com.rainy.homebudgettracker.mapper;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountRequest;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.auth.UserDetailsServiceImpl;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.PaymentMethod;
import com.rainy.homebudgettracker.user.Role;
import com.rainy.homebudgettracker.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModelMapperTest {
    private ModelMapper modelMapper;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();

        var userDetailsService = mock(UserDetailsServiceImpl.class);
        when(userDetailsService.getCurrentUser()).thenReturn(user);

        modelMapper = new ModelMapper(userDetailsService);
    }

    @Test
    public void shouldMapCategoryToCategoryResponse() {
        var category = Category.builder()
                .id(1L)
                .name("Food")
                .user(user)
                .build();

        var categoryResponse = modelMapper.map(category, CategoryResponse.class);

        assertEquals(category.getId(), categoryResponse.getId());
        assertEquals(category.getName(), categoryResponse.getName());
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

        var category = modelMapper.map(categoryRequest, Category.class);

        assertEquals(categoryRequest.getName(), category.getName());
        assertNotNull(category.getUser());
        assertEquals(user.getId(), category.getUser().getId());
    }

    @Test
    public void shouldThrowExceptionCategory() {
        var exception = assertThrows(
                UnsupportedOperationException.class, () -> modelMapper.map(new Object(), CategoryRequest.class));
        assertEquals("Mapping not supported", exception.getMessage());
    }

    @Test
    public void shouldMapAccountToAccountResponse() {
        var account = Account.builder()
                .id(1L)
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .user(user)
                .build();

        var accountResponse = modelMapper.map(account, AccountResponse.class);

        assertEquals(account.getId(), accountResponse.getId());
        assertEquals(account.getName(), accountResponse.getName());
        assertEquals(account.getCurrencyCode().name(), accountResponse.getCurrencyCode());
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

        var account = modelMapper.map(accountRequest, Account.class);

        assertEquals(accountRequest.getName(), account.getName());
        assertEquals(accountRequest.getCurrencyCode(), account.getCurrencyCode());
        assertNotNull(account.getUser());
        assertEquals(user.getId(), account.getUser().getId());
    }

    @Test
    public void shouldThrowExceptionAccount() {
        var exception = assertThrows(
                UnsupportedOperationException.class, () -> modelMapper.map(new Object(), AccountRequest.class));
        assertEquals("Mapping not supported", exception.getMessage());
    }

    @Test
    public void TransactionRequestToTransaction() {
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

        var transaction = modelMapper.mapTransactionRequestToTransaction(transactionRequest, account, category);

        assertEquals(transactionRequest.getAmount(), transaction.getAmount());
        assertEquals(category, transaction.getCategory());
        assertEquals(transactionRequest.getDate(), transaction.getDate());
        assertEquals(account, transaction.getAccount());
        assertEquals(transactionRequest.getPaymentMethod(), transaction.getPaymentMethod());
        assertEquals(user, transaction.getUser());
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

        var transaction = modelMapper.mapTransactionRequestToTransaction(transactionRequest, account, category);

        assertEquals(transactionRequest.getAmount(), transaction.getAmount());
        assertEquals(category, transaction.getCategory());
        assertEquals(transactionRequest.getDate(), transaction.getDate());
        assertEquals(account, transaction.getAccount());
        assertEquals(transactionRequest.getPaymentMethod(), transaction.getPaymentMethod());
        assertEquals(user, transaction.getUser());
    }
}