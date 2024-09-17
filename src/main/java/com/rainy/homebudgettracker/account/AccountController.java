package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<Iterable<AccountResponse>> getAllAccountsByCurrentUser() {
        return ResponseEntity.ok(accountService.findAllByCurrentUser());
    }

    @GetMapping("/{code}")
    public ResponseEntity<AccountResponse> getAccountByCurrentUserAndCurrencyCode(
            @PathVariable("code") String code
    ) throws RecordDoesNotExistException {
        CurrencyCode currencyCode = CurrencyCode.valueOf(code.toUpperCase());
        return ResponseEntity.ok(accountService.findOneAsResponseByCurrentUserAndCurrencyCode(currencyCode));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccountForCurrentUser(
            @RequestBody @Valid AccountRequest accountRequest) throws RecordAlreadyExistsException {
        return ResponseEntity.ok(accountService.createAccountForCurrentUser(accountRequest));
    }

    @PatchMapping
    public ResponseEntity<AccountResponse> updateCurrentUserAccountName(
            @RequestBody @Valid AccountRequest accountRequest) throws RecordDoesNotExistException {
        return ResponseEntity.ok(accountService.updateCurrentUserAccountName(accountRequest));
    }
}
