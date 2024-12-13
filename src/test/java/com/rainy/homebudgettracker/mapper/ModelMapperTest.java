package com.rainy.homebudgettracker.mapper;

import com.rainy.homebudgettracker.account.Account;
import com.rainy.homebudgettracker.account.AccountRequest;
import com.rainy.homebudgettracker.account.AccountResponse;
import com.rainy.homebudgettracker.category.Category;
import com.rainy.homebudgettracker.category.CategoryRequest;
import com.rainy.homebudgettracker.category.CategoryResponse;
import com.rainy.homebudgettracker.transaction.Transaction;
import com.rainy.homebudgettracker.transaction.TransactionRequest;
import com.rainy.homebudgettracker.transaction.TransactionResponse;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.transaction.enums.TransactionMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ModelMapperTest {
    @InjectMocks
    ModelMapper modelMapper;
    String userSub = "550e8400-e29b-41d4-a716-446655440000";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void shouldMapCategoryToCategoryResponse() {
        var category = Category.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Food")
                .userSub(userSub)
                .build();

        var returnedCategoryResponse = modelMapper.map(category, CategoryResponse.class);

        var categoryResponse = CategoryResponse.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
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

        var returnedCategory = modelMapper.map(categoryRequest, Category.class, userSub);

        var category = Category.builder()
                .name("Food")
                .userSub(userSub)
                .build();

        assertEquals(category, returnedCategory);
    }

    @Test
    public void shouldThrowExceptionCategory() {
        var exception = assertThrows(
                UnsupportedOperationException.class,
                () -> modelMapper.map(new Object(), CategoryRequest.class, userSub));
        assertEquals("Mapping not supported", exception.getMessage());
    }

    @Test
    public void shouldMapAccountToAccountResponse() {
        var account = Account.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .userSub(userSub)
                .build();

        var returnedAccountResponse = modelMapper.map(account, AccountResponse.class);

        var accountResponse = AccountResponse.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Main")
                .currencyCode(CurrencyCode.USD.name())
                .build();

        assertEquals(accountResponse, returnedAccountResponse);
    }

    @Test
    public void shouldMapAccountToAccountResponseWithBalance() {
        var account = Account.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .userSub(userSub)
                .build();
        var balance = BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);

        var returnedAccountResponse = modelMapper.map(account, AccountResponse.class, balance);

        var accountResponse = AccountResponse.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Main")
                .currencyCode(CurrencyCode.USD.name())
                .balance("100.00")
                .build();

        assertEquals(accountResponse, returnedAccountResponse);
    }

    @Test
    public void shouldThrowExceptionAccountResponse() {
        var exception = assertThrows(
                UnsupportedOperationException.class,
                () -> modelMapper.map(new Object(), AccountResponse.class, userSub));
        assertEquals("Mapping not supported", exception.getMessage());
    }

    @Test
    public void shouldMapAccountRequestToAccount() {
        var accountRequest = com.rainy.homebudgettracker.account.AccountRequest.builder()
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .build();

        var returnedAccount = modelMapper.map(accountRequest, Account.class, userSub);

        var account = Account.builder()
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .userSub(userSub)
                .build();

        assertEquals(account, returnedAccount);
    }

    @Test
    public void shouldThrowExceptionAccount() {
        var exception = assertThrows(
                UnsupportedOperationException.class,
                () -> modelMapper.map(new Object(), AccountRequest.class, userSub));
        assertEquals("Mapping not supported", exception.getMessage());
    }

    @Test
    public void shouldMapTransactionRequestToTransaction1() {
        var userSub = "550e8400-e29b-41d4-a716-446655440000";

        var account = Account.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .userSub(userSub)
                .build();
        var category = Category.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Food")
                .userSub(userSub)
                .build();

        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .date(LocalDate.now())
                .transactionMethod(TransactionMethod.CASH)
                .categoryName(CategoryRequest.builder().name("Food").build())
                .currencyCode(CurrencyCode.USD)
                .details("Details")
                .build();

        var returnedTransaction = modelMapper.map(transactionRequest, Transaction.class, userSub, category, account);

        var transaction = Transaction.builder()
                .amount(transactionRequest.getAmount().setScale(2, RoundingMode.HALF_UP))
                .category(category)
                .date(transactionRequest.getDate())
                .account(account)
                .transactionMethod(transactionRequest.getTransactionMethod())
                .userSub(userSub)
                .details(transactionRequest.getDetails())
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
    public void shouldMapTransactionRequestToTransaction2() {
        var userSub = "550e8400-e29b-41d4-a716-446655440000";

        var account = Account.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .userSub(userSub)
                .build();
        var category = Category.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Food")
                .userSub(userSub)
                .build();

        var transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .date(LocalDate.now())
                .transactionMethod(TransactionMethod.CASH)
                .categoryName(CategoryRequest.builder().name("Food").build())
                .currencyCode(CurrencyCode.USD)
                .build();

        var returnedTransaction = modelMapper.map(transactionRequest, Transaction.class, userSub, category, account);

        var transaction = Transaction.builder()
                .amount(transactionRequest.getAmount().setScale(2, RoundingMode.HALF_UP))
                .category(category)
                .date(transactionRequest.getDate())
                .account(account)
                .transactionMethod(transactionRequest.getTransactionMethod())
                .userSub(userSub)
                .build();

        assertEquals(transaction, returnedTransaction);
    }

    @Test
    public void shouldMapTransactionToTransactionResponse() {
        var userSub = "550e8400-e29b-41d4-a716-446655440000";

        var account = Account.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Main")
                .currencyCode(CurrencyCode.USD)
                .userSub(userSub)
                .build();
        var category = Category.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .name("Food")
                .userSub(userSub)
                .build();
        var imageUrl = "link-to-image";

        var transaction = Transaction.builder()
                .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                .amount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP))
                .date(LocalDate.now())
                .transactionMethod(TransactionMethod.CASH)
                .category(category)
                .account(account)
                .userSub(userSub)
                .details("Details")
                .build();

        var returnedTransactionResponse = modelMapper.map(transaction, TransactionResponse.class, imageUrl);

        var transactionResponse = TransactionResponse.builder()
                .id(transaction.getId())
                .amount("100.00")
                .category(CategoryResponse.builder()
                        .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                        .name("Food")
                        .build())
                .date(String.valueOf(transaction.getDate()))
                .account(AccountResponse.builder()
                        .id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))
                        .name("Main")
                        .currencyCode(CurrencyCode.USD.name())
                        .build())
                .transactionMethod(transaction.getTransactionMethod().name())
                .details("Details")
                .imageUrl(imageUrl)
                .build();

        assertEquals(transactionResponse, returnedTransactionResponse);
    }
}