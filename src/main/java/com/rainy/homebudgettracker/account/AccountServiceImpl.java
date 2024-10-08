package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.user.UserService;
import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public List<AccountResponse> findAllByCurrentUser() {
        User user = userService.getCurrentUser();
        Iterable<Account> accounts = accountRepository.findAllByUser(user);

        List<AccountResponse> accountResponses = new ArrayList<>();
        accounts.forEach(account -> accountResponses.add(modelMapper.map(account, AccountResponse.class)));
        return accountResponses;
    }

    @Override
    public AccountResponse findOneAsResponseByCurrentUserAndCurrencyCode(CurrencyCode currencyCode)
            throws RecordDoesNotExistException
    {
            User user = userService.getCurrentUser();
            Account account = accountRepository.findByUserAndCurrencyCode(user, currencyCode).orElseThrow(
                    () -> new RecordDoesNotExistException("Account with currency code " + currencyCode + " does not exist."));
            return modelMapper.map(account, AccountResponse.class);
    }

    @Override
    public Account findOneByCurrentUserAndCurrencyCode(CurrencyCode currencyCode)
            throws RecordDoesNotExistException
    {
        User user = userService.getCurrentUser();
        return accountRepository.findByUserAndCurrencyCode(user, currencyCode).orElseThrow(
                () -> new RecordDoesNotExistException("Account with currency code " + currencyCode + " does not exist.")
        );
    }

    @Override
    public AccountResponse createAccountForCurrentUser(AccountRequest accountRequest) throws RecordAlreadyExistsException {
        User user = userService.getCurrentUser();
        if (accountRepository.existsByUserAndCurrencyCode(user, accountRequest.getCurrencyCode())) {
            throw new RecordAlreadyExistsException("Account with currency code " + accountRequest.getCurrencyCode() + " already exists.");
        } else {
            Account account = modelMapper.map(accountRequest, Account.class);
            accountRepository.save(account);
            return modelMapper.map(account, AccountResponse.class);
        }
    }

    @Transactional
    @Override
    public AccountResponse updateCurrentUserAccountName(AccountRequest accountRequest)
            throws RecordDoesNotExistException
    {
        User user = userService.getCurrentUser();
        String name = accountRequest.getName();
        CurrencyCode currencyCode = accountRequest.getCurrencyCode();
        Optional<Account> account = accountRepository.findByUserAndCurrencyCode(user, currencyCode);
        if (account.isEmpty()) {
            throw new RecordDoesNotExistException("Account with currency code " + currencyCode + " does not exist.");
        } else {
            accountRepository.updateAccountName(user, name, currencyCode);
            Account updatedAccount = account.get();
            updatedAccount.setName(name);
            return modelMapper.map(updatedAccount, AccountResponse.class);
        }
    }
}
