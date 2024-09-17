package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import com.rainy.homebudgettracker.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<Iterable<AccountResponse>> getAllAccountsByUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(accountService.findAllByUser(user));
    }

    @GetMapping("/{code}")
    public ResponseEntity<AccountResponse> getAccountByUserAndCurrencyCode(
            @PathVariable("code") String code
    ) throws RecordDoesNotExistException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(accountService.findByUserAndCurrencyCode(user, currencyCode));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestBody @Valid AccountRequest accountRequest
    ) throws RecordAlreadyExistsException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(accountService.createAccount(user, accountRequest));
    }

    @PatchMapping
    public ResponseEntity<AccountResponse> updateAccountName(
            @RequestBody @Valid AccountRequest accountRequest
    ) throws RecordDoesNotExistException
    {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(accountService.updateAccountName(user, accountRequest));
    }
}
