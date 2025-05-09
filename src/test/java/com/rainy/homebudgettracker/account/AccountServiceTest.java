package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import com.rainy.homebudgettracker.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {
    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(userService.getUserSub()).thenReturn(TestData.USER_SUB_1);
        when(accountRepository.findById(TestData.ACCOUNT_1.getId())).thenReturn(Optional.of(TestData.ACCOUNT_1));
        when(accountRepository.findById(TestData.ACCOUNT_OTHER_USER.getId())).thenReturn(Optional.of(TestData.ACCOUNT_OTHER_USER));
        when(accountRepository.findAllByUserSub(TestData.USER_SUB_1)).thenReturn(List.of(TestData.ACCOUNT_1));
        when(accountRepository.findAllByUserSub(TestData.USER_SUB_2)).thenReturn(List.of());

        when(transactionRepository.sumAmountByAccount(any(Account.class))).thenReturn(BigDecimal.ZERO);

        when(modelMapper.map(any(Account.class), eq(AccountResponse.class)))
                .thenAnswer(invocation -> {
                    Account account = invocation.getArgument(0);
                    return new AccountResponse(account.getId(), account.getName(), account.getCurrencyCode().toString(), "0.00");
                });

        when(accountRepository.save(any(Account.class))).thenReturn(TestData.ACCOUNT_2);
    }

    @Test
    void shouldReturnAccountList() {
        var returnedAccounts = accountService.findCurrentUserAccounts();
        assertEquals(List.of(TestData.ACCOUNT_1), returnedAccounts);
    }

    @Test
    void shouldReturnEmptyAccountList() {
        when(userService.getUserSub()).thenReturn(TestData.USER_SUB_2);
        var returnedAccounts = accountService.findCurrentUserAccounts();
        assertEquals(List.of(), returnedAccounts);
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionAccountResponse() {
        UUID nonExistentId = UUID.randomUUID();
        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.findCurrentUserAccountAsResponse(nonExistentId));
    }

    @Test
    void shouldThrowUserIsNotOwnerException() {
        assertThrows(UserIsNotOwnerException.class,
                () -> accountService.findCurrentUserAccountAsResponse(TestData.ACCOUNT_OTHER_USER.getId()));
    }

    @Test
    void shouldReturnAccount() throws RecordDoesNotExistException, UserIsNotOwnerException {
        var returnedAccount = accountService.findCurrentUserAccount(TestData.ACCOUNT_1.getId());
        assertEquals(TestData.ACCOUNT_1, returnedAccount);
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionAccount() {
        UUID nonExistentId = UUID.randomUUID();
        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.findCurrentUserAccount(nonExistentId));
    }

    @Test
    void shouldReturnAccountResponseWhenAccountIsCreated() {
        when(modelMapper.map(eq(TestData.ACCOUNT_REQUEST_2), eq(Account.class), any(String.class)))
                .thenReturn(TestData.ACCOUNT_2);

        var returnedAccountResponse = accountService.createAccountForCurrentUser(TestData.ACCOUNT_REQUEST_2);
        assertEquals(TestData.ACCOUNT_RESPONSE_2, returnedAccountResponse);
    }

    @Test
    void shouldReturnAccountResponseWhenAccountNameIsUpdated()
            throws RecordDoesNotExistException, UserIsNotOwnerException {
        var accountUpdateNameRequest = new AccountUpdateNameRequest(TestData.ACCOUNT_1.getId(), "Changed name");

        doNothing().when(accountRepository).updateAccountName(accountUpdateNameRequest.getId(), accountUpdateNameRequest.getName());

        when(accountRepository.findById(accountUpdateNameRequest.getId())).thenReturn(Optional.of(TestData.ACCOUNT_1));

        var returnedAccountResponse = accountService.updateCurrentUserAccountName(accountUpdateNameRequest);

        assertEquals(TestData.ACCOUNT_RESPONSE_UPDATED, returnedAccountResponse);
    }

    @Test
    void shouldThrowRecordDoesNotExistExceptionWhenUpdatingName() {
        UUID nonExistentId = UUID.randomUUID();
        var accountUpdateNameRequest = new AccountUpdateNameRequest(nonExistentId, "Changed name");

        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RecordDoesNotExistException.class,
                () -> accountService.updateCurrentUserAccountName(accountUpdateNameRequest));
    }
}
