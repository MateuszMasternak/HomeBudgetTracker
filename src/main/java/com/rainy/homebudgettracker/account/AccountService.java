package com.rainy.homebudgettracker.account;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<Account> findCurrentUserAccounts();

    List<AccountResponse> findCurrentUserAccountsAsResponses();

    AccountResponse findCurrentUserAccountAsResponse(UUID id);

    Account findCurrentUserAccount(UUID id);

    AccountResponse createAccountForCurrentUser(AccountRequest request);

    AccountResponse updateCurrentUserAccountName(AccountUpdateNameRequest request);

    void deleteCurrentUserAccount(UUID id);
}
