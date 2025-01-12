package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.transaction.BigDecimalNormalization;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import com.rainy.homebudgettracker.user.UserService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {
    @InjectMocks
    AccountServiceImpl accountService;
    @Mock
    AccountRepository accountRepository;
    @Mock
    UserService userService;
    @Mock
    ModelMapper modelMapper;
    @Mock
    TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(userService.getUserSub()).thenReturn(TestData.userSubs.get(0));

        when(modelMapper.map(
                eq(TestData.accounts.get(0)),
                eq(AccountResponse.class),
                any(BigDecimal.class)))
                .thenReturn(TestData.accountResponses.get(0));

        when(accountRepository.findAllByUserSub(TestData.userSubs.get(0)))
                .thenReturn(List.of(TestData.accounts.get(0)));

        when(transactionRepository.sumAmountByAccount(TestData.accounts.get(0)))
                .thenReturn(BigDecimal.ZERO);

        when(accountRepository.findById(TestData.accounts.get(0).getId()))
                .thenReturn(Optional.of(TestData.accounts.get(0)));



    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void shouldReturnListWithAccountResponse() {
        try (MockedStatic<BigDecimalNormalization> normalization = Mockito.mockStatic(BigDecimalNormalization.class)) {
            normalization.when(() -> BigDecimalNormalization.normalize(any(), eq(2)))
                    .thenReturn(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

            var returnedAccountResponses = accountService.findCurrentUserAccountsAsResponses();

            assertEquals(1, returnedAccountResponses.size());
            assertEquals(TestData.accountResponses.get(0), returnedAccountResponses.get(0));
        }
    }

    @Test
    void shouldReturnAccountResponse() throws RecordDoesNotExistException, UserIsNotOwnerException {
        try (MockedStatic<BigDecimalNormalization> normalization = Mockito.mockStatic(BigDecimalNormalization.class)) {
            normalization.when(() -> BigDecimalNormalization.normalize(any(), eq(2)))
                    .thenReturn(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));

            var returnedAccountResponse = accountService.findCurrentUserAccountAsResponse(TestData.accounts.get(0).getId());

            assertEquals(TestData.accountResponses.get(0), returnedAccountResponse);
        }
    }
//
//    @Test
//    void shouldThrowRecordDoesNotExistExceptionAccountResponse() {
//        assertThrows(RecordDoesNotExistException.class,
//                () -> accountService.findCurrentUserAccountAsResponse(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903")));
//    }
//
//    @Test
//    void shouldThrowUserIsNotOwnerException() {
//        assertThrows(UserIsNotOwnerException.class,
//                () -> accountService.findCurrentUserAccountAsResponse(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773")));
//    }
//
//    @Test
//    void shouldReturnAccount() throws RecordDoesNotExistException, UserIsNotOwnerException {
//        var returnedAccount = accountService.findCurrentUserAccount(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"));
//
//        var userSub = "550e8400-e29b-41d4-a716-446655440000";
//
//        var account = Account.builder()
//                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
//                .name("USD account")
//                .currencyCode(CurrencyCode.USD)
//                .userSub(userSub)
//                .build();
//
//        assertEquals(account, returnedAccount);
//    }
//
//    @Test
//    void shouldThrowRecordDoesNotExistExceptionAccount() {
//        assertThrows(RecordDoesNotExistException.class,
//                () -> accountService.findCurrentUserAccount(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903")));
//    }
//
//    @Test
//    void shouldReturnAccountResponseWhenAccountIsCreated() {
//        var accountRequest = AccountRequest.builder()
//                .name("EUR account")
//                .currencyCode(CurrencyCode.EUR)
//                .build();
//
//        var returnedAccountResponse = accountService.createAccountForCurrentUser(accountRequest);
//
//        var accountResponse = AccountResponse.builder()
//                .id(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773"))
//                .name("EUR account")
//                .currencyCode(CurrencyCode.EUR.toString())
//                .balance("0.00")
//                .build();
//
//        assertEquals(accountResponse, returnedAccountResponse);
//    }
//
//    @Test
//    void shouldReturnAccountResponseWhenAccountNameIsUpdated()
//            throws RecordDoesNotExistException, UserIsNotOwnerException {
//
//        var returnedAccountResponse = accountService.updateCurrentUserAccountName(
//                AccountUpdateNameRequest.builder().id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100")).name("Changed name").build());
//
//        var accountResponse = AccountResponse.builder()
//                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
//                .name("Changed name")
//                .currencyCode(CurrencyCode.USD.toString())
//                .balance("0.00")
//                .build();
//
//        assertEquals(accountResponse, returnedAccountResponse);
//    }
//
//    @Test
//    void shouldThrowRecordDoesNotExistExceptionWhenUpdatingName() {
//        assertThrows(RecordDoesNotExistException.class,
//                () -> accountService.updateCurrentUserAccountName(
//                        AccountUpdateNameRequest.builder().id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903")).name("Changed name").build()));
//    }
}