package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.auth.UserDetailsServiceImpl;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.user.Role;
import com.rainy.homebudgettracker.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {
    private AccountService accountService;
    private User user;
    private Account account;
    private Account account2;
    private AccountRequest accountRequest;
    private AccountRequest accountRequest2;

    @BeforeEach
    void setUp() {
        var accountRepository = mock(AccountRepository.class);
        var userDetailsService = mock(UserDetailsServiceImpl.class);
        var modelMapper = mock(ModelMapper.class);

        user = User.builder()
                .id(1L)
                .email("mail@mail.com")
                .password("password")
                .role(Role.USER)
                .build();
        when(userDetailsService.getCurrentUser()).thenReturn(user);

        account = Account.builder()
                .id(1L)
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .user(user)
                .build();

        account2 = Account.builder()
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

        accountRequest = AccountRequest.builder()
                .name("USD account")
                .currencyCode(CurrencyCode.USD)
                .build();

        accountRequest2 = AccountRequest.builder()
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

        accountService = new AccountServiceImpl(accountRepository, userDetailsService, modelMapper);
    }

    @Test
    void shouldReturnListWithAccountResponse() {
        List<AccountResponse> accountResponses = accountService.findAllByCurrentUser();
        assertEquals(1, accountResponses.size());
        assertEquals(account.getId(), accountResponses.get(0).getId());
        assertEquals(account.getName(), accountResponses.get(0).getName());
        assertEquals(account.getCurrencyCode().name(), accountResponses.get(0).getCurrencyCode());
    }

    @Test
    void shouldReturnAccountResponse() throws RecordDoesNotExistException {
        AccountResponse accountResponse = accountService.findOneAsResponseByCurrentUserAndCurrencyCode(CurrencyCode.USD);
        assertEquals(account.getId(), accountResponse.getId());
        assertEquals(account.getName(), accountResponse.getName());
        assertEquals(account.getCurrencyCode().name(), accountResponse.getCurrencyCode());
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionAccountResponse() {
        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.findOneAsResponseByCurrentUserAndCurrencyCode(CurrencyCode.EUR));
    }

    @Test
    void shouldReturnAccount() throws RecordDoesNotExistException {
        Account account = accountService.findOneByCurrentUserAndCurrencyCode(CurrencyCode.USD);
        assertEquals(this.account, account);
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionAccount() {
        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.findOneByCurrentUserAndCurrencyCode(CurrencyCode.EUR));
    }

    @Test
    void shouldReturnAccountResponseWhenAccountIsCreated() throws RecordAlreadyExistsException {
        AccountResponse accountResponse = accountService.createAccountForCurrentUser(accountRequest2);
        assertEquals(account2.getId(), accountResponse.getId());
        assertEquals(account2.getName(), accountResponse.getName());
        assertEquals(account2.getCurrencyCode().name(), accountResponse.getCurrencyCode());
    }

    @Test
    void shouldThrowRecordAlreadyExistsExceptionWhenAccountAlreadyExists() {
        assertThrows(RecordAlreadyExistsException.class,
                () -> accountService.createAccountForCurrentUser(accountRequest));
    }

    @Test
    void shouldReturnAccountResponseWhenAccountNameIsUpdated() throws RecordDoesNotExistException {
        AccountResponse accountResponse = accountService.updateCurrentUserAccountName(
                AccountRequest.builder().name("Changed name").currencyCode(CurrencyCode.USD).build());
        assertEquals(account.getId(), accountResponse.getId());
        assertEquals("Changed name", accountResponse.getName());
        assertEquals(account.getCurrencyCode().name(), accountResponse.getCurrencyCode());
    }
}