package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.transaction.AccountBalance;
import com.rainy.homebudgettracker.transaction.TransactionRepository;
import com.rainy.homebudgettracker.user.UserService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.rainy.homebudgettracker.transaction.BigDecimalNormalization.normalize;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public List<Account> findCurrentUserAccounts() {
        String userSub = userService.getUserSub();
        return (List<Account>) accountRepository.findAllByUserSub(userSub);
    }

    @Override
    public List<AccountResponse> findCurrentUserAccountsAsResponses() {
        String userSub = userService.getUserSub();
        List<Account> accounts = (List<Account>) accountRepository.findAllByUserSub(userSub);

        Map<UUID, BigDecimal> balances = transactionRepository.getBalancesForUserAccounts(userSub).stream()
                .collect(Collectors.toMap(
                        AccountBalance::accountId,
                        AccountBalance::balance
                ));

        return accounts.stream()
                .map(account -> {
                    BigDecimal balance = balances.getOrDefault(account.getId(), BigDecimal.ZERO);
                    return modelMapper.map(account, AccountResponse.class, normalize(balance, 2));
                })
                .collect(Collectors.toList());
    }

    @Override
    public AccountResponse findCurrentUserAccountAsResponse(UUID id) {
        Account account = findAndVerifyAccountOwner(id);
        BigDecimal balance = normalize(transactionRepository.sumAmountByAccount(account), 2);
        return modelMapper.map(account, AccountResponse.class, balance);
    }

    @Override
    public Account findCurrentUserAccount(UUID id) {
        return findAndVerifyAccountOwner(id);
    }

    private Account findAndVerifyAccountOwner(UUID id) {
        String userSub = userService.getUserSub();
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RecordDoesNotExistException("Account with id " + id + " does not exist"));
        if (!account.getUserSub().equals(userSub)) {
            throw new UserIsNotOwnerException("User is not the owner of the Account with id " + id);
        }
        return account;
    }

    @Override
    public AccountResponse createAccountForCurrentUser(AccountRequest request) {
        String userSub = userService.getUserSub();
        Account account = modelMapper.map(request, Account.class, userSub);
        accountRepository.save(account);
        return modelMapper.map(account, AccountResponse.class);
    }

    @Transactional
    @Override
    public AccountResponse updateCurrentUserAccountName(AccountUpdateNameRequest request) {
        Account account = findAndVerifyAccountOwner(request.getId());
        account.setName(request.getName());
        Account updatedAccount = accountRepository.save(account);
        return modelMapper.map(updatedAccount, AccountResponse.class);
    }

    @Override
    public void deleteCurrentUserAccount(UUID id) {
        Account account = findCurrentUserAccount(id);
        accountRepository.delete(account);
    }
}
