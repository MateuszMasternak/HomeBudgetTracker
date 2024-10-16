package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
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

    @GetMapping("/all")
    public ResponseEntity<Iterable<AccountResponse>> getAllAccountsByCurrentUser() {
        return ResponseEntity.ok(accountService.findCurrentUserAccountsAsResponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountByCurrentUserAndCurrencyCode(
            @PathVariable() Long id
    ) throws RecordDoesNotExistException, UserIsNotOwnerException {
        return ResponseEntity.ok(accountService.findCurrentUserAccountAsResponseById(id));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccountForCurrentUser(
            @RequestBody @Valid AccountRequest request) throws RecordAlreadyExistsException {
        return ResponseEntity.ok(accountService.createAccountForCurrentUser(request));
    }

    @PatchMapping
    public ResponseEntity<AccountResponse> updateCurrentUserAccountName(
            @RequestBody @Valid AccountUpdateNameRequest request
    ) throws RecordDoesNotExistException, UserIsNotOwnerException {
        return ResponseEntity.ok(accountService.updateCurrentUserAccountName(request));
    }
}
