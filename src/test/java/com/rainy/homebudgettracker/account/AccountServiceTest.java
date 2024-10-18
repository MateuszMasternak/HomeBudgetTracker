package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.UserService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        var userSub = "550e8400-e29b-41d4-a716-446655440000";
        when(userService.getUserSub()).thenReturn(userSub);

        var userSub2 = "550e8400-e29b-41d4-a716-446655440001";

        var account = Account.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .userSub(userSub)
                .build();

        var account2 = Account.builder()
                .id(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773"))
                .name("EUR account")
                .currencyCode(CurrencyCode.EUR)
                .userSub(userSub)
                .build();

        var account3 = Account.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("Changed name")
                .currencyCode(CurrencyCode.USD)
                .userSub(userSub)
                .build();

        var account4 = Account.builder()
                .id(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773"))
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .userSub(userSub2)
                .build();

        var accountRequest = AccountRequest.builder()
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .build();

        var accountRequest2 = AccountRequest.builder()
                .name("EUR account")
                .currencyCode(CurrencyCode.EUR)
                .build();

        when(modelMapper.map(account, AccountResponse.class)).thenReturn(AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .currencyCode(account.getCurrencyCode().toString())
                .build());
        when(modelMapper.map(account2, AccountResponse.class)).thenReturn(AccountResponse.builder()
                .id(account2.getId())
                .name(account2.getName())
                .currencyCode(account2.getCurrencyCode().toString())
                .build());
        when(modelMapper.map(account3, AccountResponse.class)).thenReturn(AccountResponse.builder()
                .id(account3.getId())
                .name(account3.getName())
                .currencyCode(account3.getCurrencyCode().toString())
                .build());
        when(modelMapper.map(accountRequest, Account.class)).thenReturn(account);
        when(modelMapper.map(accountRequest2, Account.class)).thenReturn(account2);

        when(accountRepository.findAllByUserSub(userSub)).thenReturn(List.of(account));
        when(accountRepository.findById(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))).thenReturn(Optional.of(account));
        when(accountRepository.findById(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773"))).thenReturn(Optional.of(account4));
        when(accountRepository.findById(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903"))).thenReturn(Optional.empty());
        when(accountRepository.existsById(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))).thenReturn(true);
        when(accountRepository.existsById(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773"))).thenReturn(false);
        when(accountRepository.save(account2)).thenReturn(account2);
        doNothing().when(accountRepository).updateAccountName(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"), "Changed name");
    }

    @AfterEach
    void tearDown() {
        clearInvocations(accountRepository);
    }

    @Test
    void shouldReturnListWithAccountResponse() {
        var accountResponses = accountService.findCurrentUserAccountsAsResponses();

        var account = Account.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .build();

        assertEquals(1, accountResponses.size());
        assertEquals(account.getId(), accountResponses.get(0).getId());
        assertEquals(account.getName(), accountResponses.get(0).getName());
        assertEquals(account.getCurrencyCode().name(), accountResponses.get(0).getCurrencyCode());
    }

    @Test
    void shouldReturnAccountResponse() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var accountResponse = accountService.findCurrentUserAccountAsResponse(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"));

        var account = Account.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .build();

        assertEquals(account.getId(), accountResponse.getId());
        assertEquals(account.getName(), accountResponse.getName());
        assertEquals(account.getCurrencyCode().name(), accountResponse.getCurrencyCode());
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionAccountResponse() {
        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.findCurrentUserAccountAsResponse(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903")));
    }

    @Test
    void shouldThrowUserIsNotOwnerException() {
        assertThrows(UserIsNotOwnerException.class,
                () -> accountService.findCurrentUserAccountAsResponse(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773")));
    }

    @Test
    void shouldReturnAccount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var returnedAccount = accountService.findCurrentUserAccount(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"));

        var userSub = "550e8400-e29b-41d4-a716-446655440000";

        var account = Account.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .userSub(userSub)
                .build();

        assertEquals(account, returnedAccount);
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionAccount() {
        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.findCurrentUserAccount(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903")));
    }

    @Test
    void shouldReturnAccountResponseWhenAccountIsCreated() {
        var accountRequest = AccountRequest.builder()
                .name("EUR account")
                .currencyCode(CurrencyCode.EUR)
                .build();

        var accountResponse = accountService.createAccountForCurrentUser(accountRequest);

        var account = Account.builder()
                .id(UUID.fromString("c7e2fa7e-2267-4da5-ade1-5dc79948a773"))
                .name("EUR account")
                .currencyCode(CurrencyCode.EUR)
                .build();

        assertEquals(account.getId(), accountResponse.getId());
        assertEquals(account.getName(), accountResponse.getName());
        assertEquals(account.getCurrencyCode().name(), accountResponse.getCurrencyCode());
    }

    @Test
    void shouldReturnAccountResponseWhenAccountNameIsUpdated()
            throws RecordDoesNotExistException, UserIsNotOwnerException {

        var accountResponse = accountService.updateCurrentUserAccountName(
                AccountUpdateNameRequest.builder().id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100")).name("Changed name").build());

        var account = Account.builder()
                .id(UUID.fromString("312a1af8-a338-49ea-b67c-860062a10100"))
                .name("Changed name")
                .currencyCode(CurrencyCode.USD)
                .build();

        assertEquals(account.getId(), accountResponse.getId());
        assertEquals("Changed name", accountResponse.getName());
        assertEquals(account.getCurrencyCode().name(), accountResponse.getCurrencyCode());
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionWhenUpdatingName() {
        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.updateCurrentUserAccountName(
                        AccountUpdateNameRequest.builder().id(UUID.fromString("b848bced-0daf-4ad7-b9c6-4c477ab5a903")).name("Changed name").build()));
    }
}