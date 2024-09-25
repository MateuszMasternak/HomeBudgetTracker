package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;

import java.util.List;

public interface AccountService {
    List<AccountResponse> findAllByCurrentUser();
    AccountResponse findOneAsResponseByCurrentUserAndCurrencyCode(CurrencyCode currencyCode)
            throws RecordDoesNotExistException;
    Account findOneByCurrentUserAndCurrencyCode(CurrencyCode currencyCode)
            throws RecordDoesNotExistException;
    AccountResponse createAccountForCurrentUser(AccountRequest accountRequest)
            throws RecordAlreadyExistsException;
    AccountResponse updateCurrentUserAccountName(AccountRequest accountRequest)
            throws RecordDoesNotExistException;
}
