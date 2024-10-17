package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.UserService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.user.Role;
import com.rainy.homebudgettracker.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

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

        var user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();
        when(userService.getCurrentUser()).thenReturn(user);

        var user2 = User.builder()
                .id(2L)
                .email("other-mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();

        var account = Account.builder()
                .id(1L)
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .user(user)
                .build();

        var account2 = Account.builder()
                .id(2L)
                .name("EUR account")
                .currencyCode(CurrencyCode.EUR)
                .user(user)
                .build();

        var account3 = Account.builder()
                .id(1L)
                .name("Changed name")
                .currencyCode(CurrencyCode.USD)
                .user(user)
                .build();

        var account4 = Account.builder()
                .id(2L)
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .user(user2)
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

        when(accountRepository.findAllByUser(user)).thenReturn(List.of(account));
        when(accountRepository.findById(1L)).thenReturn(java.util.Optional.of(account));
        when(accountRepository.findById(2L)).thenReturn(java.util.Optional.of(account4));
        when(accountRepository.findById(3L)).thenReturn(java.util.Optional.empty());
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.existsById(2L)).thenReturn(false);
        when(accountRepository.save(account2)).thenReturn(account2);
        doNothing().when(accountRepository).updateAccountName(1L, "Changed name");
    }

    @AfterEach
    void tearDown() {
        clearInvocations(accountRepository);
    }

    @Test
    void shouldReturnListWithAccountResponse() {
        var accountResponses = accountService.findCurrentUserAccountsAsResponses();

        var account = Account.builder()
                .id(1L)
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .build();

        assertEquals(1, accountResponses.size());
        assertEquals(account.getId(), accountResponses.get(0).getId());
        assertEquals(account.getName(), accountResponses.get(0).getName());
        assertEquals(account.getCurrencyCode().name(), accountResponses.get(0).getCurrencyCode());

        verify(userService, times(1)).getCurrentUser();
        verify(accountRepository, times(1)).findAllByUser(any(User.class));
        verify(modelMapper, times(1)).map(any(Account.class), eq(AccountResponse.class));
    }

    @Test
    void shouldReturnAccountResponse() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var accountResponse = accountService.findCurrentUserAccountAsResponse(1L);

        var account = Account.builder()
                .id(1L)
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
                () -> accountService.findCurrentUserAccountAsResponse(3L));
    }

    @Test
    void shouldThrowUserIsNotOwnerException() {
        assertThrows(UserIsNotOwnerException.class,
                () -> accountService.findCurrentUserAccountAsResponse(2L));
    }

    @Test
    void shouldReturnAccount() throws RecordDoesNotExistException, UserIsNotOwnerException {
//        var returnedAccount = accountService.findOneByCurrentUserAndCurrencyCode(CurrencyCode.USD);
        var returnedAccount = accountService.findCurrentUserAccount(1L);

        var user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();

        var account = Account.builder()
                .id(1L)
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .user(user)
                .build();

        assertEquals(account, returnedAccount);
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionAccount() {
        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.findCurrentUserAccount(3L));
    }

    @Test
    void shouldReturnAccountResponseWhenAccountIsCreated() {
        var accountRequest = AccountRequest.builder()
                .name("EUR account")
                .currencyCode(CurrencyCode.EUR)
                .build();

        var accountResponse = accountService.createAccountForCurrentUser(accountRequest);

        var account = Account.builder()
                .id(2L)
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
                AccountUpdateNameRequest.builder().id(1L).name("Changed name").build());

        var account = Account.builder()
                .id(1L)
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
                        AccountUpdateNameRequest.builder().id(3L).name("Changed name").build()));
    }
}