package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<Account> findCurrentUserAccounts();

    List<AccountResponse> findCurrentUserAccountsAsResponses();

    AccountResponse findCurrentUserAccountAsResponse(UUID id)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    Account findCurrentUserAccount(UUID id)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    AccountResponse createAccountForCurrentUser(AccountRequest request)
            throws RecordAlreadyExistsException;

    AccountResponse updateCurrentUserAccountName(AccountUpdateNameRequest request)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    void deleteCurrentUserAccount(UUID id) throws RecordDoesNotExistException, UserIsNotOwnerException;
}
