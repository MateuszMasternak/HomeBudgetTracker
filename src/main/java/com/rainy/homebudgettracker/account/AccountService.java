package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;

import java.util.List;

public interface AccountService {
    List<AccountResponse> findCurrentUserAccountsAsResponses();

    AccountResponse findCurrentUserAccountAsResponse(Long id)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    Account findCurrentUserAccount(Long id)
            throws RecordDoesNotExistException, UserIsNotOwnerException;

    AccountResponse createAccountForCurrentUser(AccountRequest request)
            throws RecordAlreadyExistsException;

    AccountResponse updateCurrentUserAccountName(AccountUpdateNameRequest request)
            throws RecordDoesNotExistException, UserIsNotOwnerException;
}
