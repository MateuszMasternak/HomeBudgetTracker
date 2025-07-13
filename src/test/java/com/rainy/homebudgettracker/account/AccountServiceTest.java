package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.AccountBalance;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import com.rainy.homebudgettracker.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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


    @Nested
    @DisplayName("Tests for finding accounts")
    class FindingAccountsTests {

        @Test
        @DisplayName("should return account list for current user")
        void findCurrentUserAccounts_shouldReturnAccountList() {
            String userSub = TestData.USER_SUB_1;
            Account account1 = TestData.ACCOUNT_1;
            AccountResponse expectedResponse = TestData.ACCOUNT_RESPONSE_1;

            when(userService.getUserSub()).thenReturn(userSub);
            when(accountRepository.findAllByUserSub(userSub)).thenReturn(List.of(account1));
            when(transactionRepository.getBalancesForUserAccounts(userSub)).thenReturn(List.of(new AccountBalance(account1.getId(), new BigDecimal("100.00"))));
            when(modelMapper.map(any(Account.class), any(), any(BigDecimal.class))).thenReturn(expectedResponse);

            List<AccountResponse> responses = accountService.findCurrentUserAccountsAsResponses();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getId()).isEqualTo(account1.getId());
        }

        @Test
        @DisplayName("should throw RecordDoesNotExistException when account not found")
        void findCurrentUserAccount_shouldThrowException_whenAccountNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.findCurrentUserAccount(nonExistentId))
                    .isInstanceOf(RecordDoesNotExistException.class)
                    .hasMessageContaining("Account with id " + nonExistentId + " does not exist.");
        }

        @Test
        @DisplayName("should throw UserIsNotOwnerException when user is not account owner")
        void findCurrentUserAccount_shouldThrowException_whenUserIsNotOwner() {
            String currentUserSub = TestData.USER_SUB_1;
            Account otherUserAccount = TestData.ACCOUNT_OTHER_USER;

            when(userService.getUserSub()).thenReturn(currentUserSub);
            when(accountRepository.findById(otherUserAccount.getId())).thenReturn(Optional.of(otherUserAccount));

            assertThatThrownBy(() -> accountService.findCurrentUserAccount(otherUserAccount.getId()))
                    .isInstanceOf(UserIsNotOwnerException.class)
                    .hasMessageContaining("User is not the owner of the Account with id " + otherUserAccount.getId());
        }
    }

    @Nested
    @DisplayName("Tests for updating an account")
    class UpdatingAccountTests {

        @Test
        @DisplayName("should update account name and return response")
        void updateCurrentUserAccountName_shouldUpdateNameAndReturnResponse() {
            String userSub = TestData.USER_SUB_1;
            Account accountToUpdate = TestData.ACCOUNT_1;

            AccountUpdateNameRequest request = new AccountUpdateNameRequest(accountToUpdate.getId(), "New Name");
            Account savedAccount = new Account(accountToUpdate.getId(), request.getName(), accountToUpdate.getCurrencyCode(), userSub);
            AccountResponse expectedResponse = new AccountResponse(savedAccount.getId(), savedAccount.getName(), savedAccount.getCurrencyCode().name(), null);

            when(userService.getUserSub()).thenReturn(userSub);
            when(accountRepository.findById(request.getId())).thenReturn(Optional.of(accountToUpdate));
            when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
            when(modelMapper.map(savedAccount, AccountResponse.class)).thenReturn(expectedResponse);

            AccountResponse actualResponse = accountService.updateCurrentUserAccountName(request);

            assertThat(actualResponse).isEqualTo(expectedResponse);

            ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountCaptor.capture());
            assertThat(accountCaptor.getValue().getName()).isEqualTo("New Name");
        }
    }
}