package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.auth.UserDetailsServiceImpl;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
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
    UserDetailsServiceImpl userDetailsService;
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
        when(userDetailsService.getCurrentUser()).thenReturn(user);

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
        when(accountRepository.findByUserAndCurrencyCode(user, CurrencyCode.USD)).thenReturn(java.util.Optional.of(account));
        when(accountRepository.findByUserAndCurrencyCode(user, CurrencyCode.EUR)).thenReturn(java.util.Optional.empty());
        when(accountRepository.existsByUserAndCurrencyCode(user, CurrencyCode.USD)).thenReturn(true);
        when(accountRepository.existsByUserAndCurrencyCode(user, CurrencyCode.EUR)).thenReturn(false);
        when(accountRepository.save(account2)).thenReturn(account2);
        doNothing().when(accountRepository).updateAccountName(user, "Changed name", CurrencyCode.USD);
        when(accountRepository.existsByUserAndCurrencyCode(user, CurrencyCode.USD)).thenReturn(true);
        when(accountRepository.existsByUserAndCurrencyCode(user, CurrencyCode.EUR)).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        clearInvocations(accountRepository);
    }

    @Test
    void shouldReturnListWithAccountResponse() {
        var accountResponses = accountService.findAllByCurrentUser();

        var account = Account.builder()
                .id(1L)
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .build();

        assertEquals(1, accountResponses.size());
        assertEquals(account.getId(), accountResponses.get(0).getId());
        assertEquals(account.getName(), accountResponses.get(0).getName());
        assertEquals(account.getCurrencyCode().name(), accountResponses.get(0).getCurrencyCode());

        verify(userDetailsService, times(1)).getCurrentUser();
        verify(accountRepository, times(1)).findAllByUser(any(User.class));
        verify(modelMapper, times(1)).map(any(Account.class), eq(AccountResponse.class));
    }

    @Test
    void shouldReturnAccountResponse() throws RecordDoesNotExistException {
        var accountResponse = accountService.findOneAsResponseByCurrentUserAndCurrencyCode(CurrencyCode.USD);

        var account = Account.builder()
                .id(1L)
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .build();

        assertEquals(account.getId(), accountResponse.getId());
        assertEquals(account.getName(), accountResponse.getName());
        assertEquals(account.getCurrencyCode().name(), accountResponse.getCurrencyCode());

        verifyGetOne(new int[]{1, 1});
        verify(modelMapper, times(1)).map(any(Account.class), eq(AccountResponse.class));
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionAccountResponse() {
        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.findOneAsResponseByCurrentUserAndCurrencyCode(CurrencyCode.EUR));

        verifyGetOne(new int[]{1, 1});
        verify(modelMapper, times(0)).map(any(Account.class), eq(AccountResponse.class));
    }

    @Test
    void shouldReturnAccount() throws RecordDoesNotExistException {
        var returnedAccount = accountService.findOneByCurrentUserAndCurrencyCode(CurrencyCode.USD);

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

        verifyGetOne(new int[]{1, 1});
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionAccount() {
        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.findOneByCurrentUserAndCurrencyCode(CurrencyCode.EUR));

        verifyGetOne(new int[]{1, 1});
    }

    @Test
    void shouldReturnAccountResponseWhenAccountIsCreated() throws RecordAlreadyExistsException {
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

        verifySave(new int[]{1, 1, 1, 1, 1});
    }

    @Test
    void shouldThrowRecordAlreadyExistsExceptionWhenAccountAlreadyExists() {
        var accountRequest = AccountRequest.builder()
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .build();

        assertThrows(RecordAlreadyExistsException.class,
                () -> accountService.createAccountForCurrentUser(accountRequest));

        verifySave(new int[]{1, 1, 0, 0, 0});
    }

    @Test
    void shouldReturnAccountResponseWhenAccountNameIsUpdated() throws RecordDoesNotExistException {
        var accountResponse = accountService.updateCurrentUserAccountName(
                AccountRequest.builder().name("Changed name").currencyCode(CurrencyCode.USD).build());

        var account = Account.builder()
                .id(1L)
                .name("Changed name")
                .currencyCode(CurrencyCode.USD)
                .build();

        assertEquals(account.getId(), accountResponse.getId());
        assertEquals("Changed name", accountResponse.getName());
        assertEquals(account.getCurrencyCode().name(), accountResponse.getCurrencyCode());

        verifyUpdate(new int[]{1, 1});
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionWhenAccountWhenUpdatingName() {
        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.updateCurrentUserAccountName(
                        AccountRequest.builder().name("Changed name").currencyCode(CurrencyCode.EUR).build()));

        verifyUpdate(new int[]{1, 0});
    }

    void verifySave(int[] times) {
        verify(userDetailsService, times(times[0])).getCurrentUser();
        verify(accountRepository, times(times[1])).existsByUserAndCurrencyCode(any(User.class), any(CurrencyCode.class));
        verify(accountRepository, times(times[2])).save(any(Account.class));
        verify(modelMapper, times(times[3])).map(any(Account.class), eq(AccountResponse.class));
        verify(modelMapper, times(times[4])).map(any(AccountRequest.class), eq(Account.class));
    }

    void verifyUpdate(int[] times) {
        verify(accountRepository, times(times[0])).findByUserAndCurrencyCode(any(User.class), any(CurrencyCode.class));
        verify(accountRepository, times(times[1])).updateAccountName(any(User.class), any(String.class), any(CurrencyCode.class));
    }

    void verifyGetOne(int[] times) {
        verify(userDetailsService, times(times[0])).getCurrentUser();
        verify(accountRepository, times(times[1])).findByUserAndCurrencyCode(any(User.class), any(CurrencyCode.class));
    }
}