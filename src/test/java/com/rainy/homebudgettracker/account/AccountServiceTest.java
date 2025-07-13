package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.AccountBalance;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
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

import static com.rainy.homebudgettracker.account.TestData.ACCOUNT_1;
import static com.rainy.homebudgettracker.account.TestData.USER_SUB_1;


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
            when(userService.getUserSub()).thenReturn(USER_SUB_1);
            when(accountRepository.findAllByUserSub(USER_SUB_1)).thenReturn(List.of(ACCOUNT_1));

            when(transactionRepository.getBalancesForUserAccounts(USER_SUB_1)).thenReturn(List.of(new AccountBalance(ACCOUNT_1.getId(), new BigDecimal("100.00"))));

            when(modelMapper.map(any(Account.class), any(), any(BigDecimal.class))).thenReturn(new AccountResponse(ACCOUNT_1.getId(), ACCOUNT_1.getName(), "PLN", "100.00"));

            List<AccountResponse> responses = accountService.findCurrentUserAccountsAsResponses();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getId()).isEqualTo(ACCOUNT_1.getId());
        }

        @Test
        @DisplayName("should throw RecordDoesNotExistException when account not found")
        void findCurrentUserAccount_shouldThrowException_whenAccountNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.findCurrentUserAccount(nonExistentId))
                    .isInstanceOf(RecordDoesNotExistException.class)
                    .hasMessageContaining("Account with id " + nonExistentId + " does not exist");
        }

        @Test
        @DisplayName("should throw UserIsNotOwnerException when user is not account owner")
        void findCurrentUserAccount_shouldThrowException_whenUserIsNotOwner() {
            Account otherUserAccount = new Account(UUID.randomUUID(), "Other's Account", CurrencyCode.EUR, "other-user-sub");
            when(userService.getUserSub()).thenReturn(USER_SUB_1);
            when(accountRepository.findById(otherUserAccount.getId())).thenReturn(Optional.of(otherUserAccount));

            assertThatThrownBy(() -> accountService.findCurrentUserAccount(otherUserAccount.getId()))
                    .isInstanceOf(UserIsNotOwnerException.class);
        }
    }

    @Nested
    @DisplayName("Tests for updating an account")
    class UpdatingAccountTests {

        @Test
        @DisplayName("should update account name and return response")
        void updateCurrentUserAccountName_shouldUpdateNameAndReturnResponse() {
            AccountUpdateNameRequest request = new AccountUpdateNameRequest(ACCOUNT_1.getId(), "New Name");
            Account savedAccount = new Account(ACCOUNT_1.getId(), request.getName(), ACCOUNT_1.getCurrencyCode(), USER_SUB_1);
            AccountResponse expectedResponse = new AccountResponse(savedAccount.getId(), savedAccount.getName(), "PLN", null);

            when(userService.getUserSub()).thenReturn(USER_SUB_1);
            when(accountRepository.findById(request.getId())).thenReturn(Optional.of(ACCOUNT_1));

            when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
            when(modelMapper.map(savedAccount, AccountResponse.class)).thenReturn(expectedResponse);

            AccountResponse actualResponse = accountService.updateCurrentUserAccountName(request);

            assertThat(actualResponse).isEqualTo(expectedResponse);
            assertThat(actualResponse.getName()).isEqualTo("New Name");

            ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(accountCaptor.capture());

            Account capturedAccount = accountCaptor.getValue();
            assertThat(capturedAccount.getName()).isEqualTo("New Name");
        }
    }
}