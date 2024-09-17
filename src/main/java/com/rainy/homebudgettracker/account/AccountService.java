package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;

public interface AccountService {
    Iterable<AccountResponse> findAllByCurrentUser();
    AccountResponse findOneAsResponseByCurrentUserAndCurrencyCode(CurrencyCode currencyCode)
            throws RecordDoesNotExistException;
    Account findOneByCurrentUserAndCurrencyCode(CurrencyCode currencyCode)
            throws RecordDoesNotExistException;
    AccountResponse createAccountForCurrentUser(AccountRequest accountRequest)
            throws RecordAlreadyExistsException;
    AccountResponse updateCurrentUserAccountName(AccountRequest accountRequest)
            throws RecordDoesNotExistException;
}
