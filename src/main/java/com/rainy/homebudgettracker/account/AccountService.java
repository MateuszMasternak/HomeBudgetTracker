package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.transaction.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Iterable<AccountResponse> findAllByUser(User user) {
        Iterable<Account> accounts = accountRepository.findAllByUser(user);

        List<AccountResponse> accountResponses = new ArrayList<>();
        for (Account account : accounts) {
            AccountResponse accountResponse = AccountResponse.builder()
                    .id(account.getId())
                    .name(account.getName())
                    .currencyCode(String.valueOf(account.getCurrencyCode()))
                    .balance(String.valueOf(account.getBalance()))
                    .build();
            accountResponses.add(accountResponse);
        }
        return accountResponses;
    }

    public AccountResponse findByUserAndCurrencyCode(User user, CurrencyCode currencyCode)
            throws RecordDoesNotExistException
    {
        try {
            Account account = accountRepository.findByUserAndCurrencyCode(user, currencyCode).orElseThrow();
            return AccountResponse.builder()
                    .id(account.getId())
                    .name(account.getName())
                    .currencyCode(String.valueOf(account.getCurrencyCode()))
                    .balance(String.valueOf(account.getBalance()))
                    .build();
        } catch (NoSuchElementException e) {
            throw new RecordDoesNotExistException("Account with currency code " + currencyCode + " does not exist.");
        }
    }

    public AccountResponse createAccount(User user, AccountRequest accountRequest) {
        Account account = Account.builder()
                .name(accountRequest.getName())
                .currencyCode(CurrencyCode.valueOf(accountRequest.getCurrencyCode()))
                .user(user)
                .build();
        accountRepository.save(account);
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .currencyCode(String.valueOf(account.getCurrencyCode()))
                .balance(String.valueOf(account.getBalance()))
                .build();
    }

    public void updateAccountBalance(User user, BigDecimal amount, CurrencyCode currencyCode)
            throws RecordDoesNotExistException {
            if (accountRepository.existsByUserAndCurrencyCode(user, currencyCode)) {
                accountRepository.updateAccountBalance(user, amount, currencyCode);
            } else {
                throw new RecordDoesNotExistException("Account with currency code " + currencyCode + " does not exist.");
            }
    }
}
