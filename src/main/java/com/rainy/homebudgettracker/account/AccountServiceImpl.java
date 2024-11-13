package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
import com.rainy.homebudgettracker.user.UserService;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.mapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public List<AccountResponse> findCurrentUserAccountsAsResponses() {
        String userSub = userService.getUserSub();
        Iterable<Account> accounts = accountRepository.findAllByUserSub(userSub);

        List<AccountResponse> accountResponses = new ArrayList<>();
        accounts.forEach(account -> accountResponses.add(modelMapper.map(account, AccountResponse.class)));
        return accountResponses;
    }

    @Override
    public AccountResponse findCurrentUserAccountAsResponse(UUID id)
            throws RecordDoesNotExistException, UserIsNotOwnerException {
            String userSub = userService.getUserSub();

            Account account = accountRepository.findById(id).orElseThrow(
                    () -> new RecordDoesNotExistException("Account with id " + id + " does not exist."));
            if (!account.getUserSub().equals(userSub))
                throw new UserIsNotOwnerException("User is not the owner of the Account with id " + id + ".");

            return modelMapper.map(account, AccountResponse.class);
    }

    @Override
    public Account findCurrentUserAccount(UUID id)
            throws RecordDoesNotExistException, UserIsNotOwnerException {
        String userSub = userService.getUserSub();

        Account account = accountRepository.findById(id).orElseThrow(
                () -> new RecordDoesNotExistException("Account with id " + id + " does not exist."));
        if (!account.getUserSub().equals(userSub))
            throw new UserIsNotOwnerException("User is not the owner of the Account with id " + id + ".");

        return account;
    }

    @Override
    public AccountResponse createAccountForCurrentUser(AccountRequest request) {
        Account account = modelMapper.map(request, Account.class);
        accountRepository.save(account);
        return modelMapper.map(account, AccountResponse.class);
    }

    @Transactional
    @Override
    public AccountResponse updateCurrentUserAccountName(AccountUpdateNameRequest request)
            throws RecordDoesNotExistException, UserIsNotOwnerException {
        Account account = findCurrentUserAccount(request.getId());
        accountRepository.updateAccountName(request.getId(), request.getName());
        account.setName(request.getName());
        return modelMapper.map(account, AccountResponse.class);
    }

    @Override
    public void deleteCurrentUserAccount(UUID id) throws RecordDoesNotExistException, UserIsNotOwnerException {
        Account account = findCurrentUserAccount(id);
        accountRepository.delete(account);
    }
}
