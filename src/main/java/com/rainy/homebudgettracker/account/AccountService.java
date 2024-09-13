package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.transaction.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Iterable<AccountResponse> findAllByUser(User user) {
        Iterable<Account> accounts = accountRepository.findAllByUser(user);

        List<AccountResponse> accountResponses = new ArrayList<>();
        for (Account account : accounts) {
            AccountResponse accountResponse = getAccountResponse(account);
            accountResponses.add(accountResponse);
        }
        return accountResponses;
    }

    public AccountResponse findByUserAndCurrencyCode(User user, CurrencyCode currencyCode)
            throws RecordDoesNotExistException
    {
        try {
            Account account = accountRepository.findByUserAndCurrencyCode(user, currencyCode).orElseThrow();
            return getAccountResponse(account);
        } catch (NoSuchElementException e) {
            throw new RecordDoesNotExistException("Account with currency code " + currencyCode + " does not exist.");
        }
    }

    public AccountResponse createAccount(User user, AccountRequest accountRequest) throws RecordAlreadyExistsException {
        if (accountRepository.existsByUserAndCurrencyCode(user, CurrencyCode.valueOf(accountRequest.getCurrencyCode().toUpperCase()))) {
            throw new RecordAlreadyExistsException("Account with currency code " + accountRequest.getCurrencyCode() + " already exists.");
        } else {
            Account account = Account.builder()
                    .name(accountRequest.getName())
                    .currencyCode(CurrencyCode.valueOf(accountRequest.getCurrencyCode().toUpperCase()))
                    .user(user)
                    .build();
            accountRepository.save(account);
            return getAccountResponse(account);
        }
    }

    @Transactional
    public AccountResponse updateAccountBalance(User user, BigDecimal amount, CurrencyCode currencyCode)
            throws RecordDoesNotExistException
    {
        Optional<Account> account = accountRepository.findByUserAndCurrencyCode(user, currencyCode);
        if (account.isEmpty()) {
            throw new RecordDoesNotExistException("Account with currency code " + currencyCode + " does not exist.");
        } else {
            accountRepository.updateAccountBalance(user, amount, currencyCode);
            Account updatedAccount = account.get();
            updatedAccount.setBalance(updatedAccount.getBalance().add(amount));
            return getAccountResponse(updatedAccount);
        }
    }

    @Transactional
    public AccountResponse updateAccountName(User user, AccountRequest accountRequest)
            throws RecordDoesNotExistException
    {
        String name = accountRequest.getName();
        CurrencyCode currencyCode = CurrencyCode.valueOf(accountRequest.getCurrencyCode().toUpperCase());
        Optional<Account> account = accountRepository.findByUserAndCurrencyCode(user, currencyCode);
        if (account.isEmpty()) {
            throw new RecordDoesNotExistException("Account with currency code " + currencyCode + " does not exist.");
        } else {
            accountRepository.updateAccountName(user, name, currencyCode);
            Account updatedAccount = account.get();
            updatedAccount.setName(name);
            return getAccountResponse(updatedAccount);
        }
    }

    private AccountResponse getAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .currencyCode(String.valueOf(account.getCurrencyCode()))
                .balance(String.valueOf(account.getBalance()))
                .build();
    }
}
