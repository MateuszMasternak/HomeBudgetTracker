package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.transaction.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public Iterable<AccountResponse> getAllAccountsByUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountService.findAllByUser(user);
    }

    @GetMapping("/{code}")
    public AccountResponse getAccountByUserAndCurrencyCode(
            @PathVariable("code") String code
    ) throws RecordDoesNotExistException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return accountService.findByUserAndCurrencyCode(user, currencyCode);
    }

    @PostMapping
    public AccountResponse createAccount(
            @RequestBody @Valid AccountRequest accountRequest)
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountService.createAccount(user, accountRequest);
    }
}
