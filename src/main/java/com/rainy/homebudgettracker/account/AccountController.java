package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.handler.exception.RecordAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.RecordDoesNotExistException;
import com.rainy.homebudgettracker.handler.exception.UserIsNotOwnerException;
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
    public ResponseEntity<Iterable<AccountResponse>> getCurrentUserAccounts() {
        return ResponseEntity.ok(accountService.findCurrentUserAccountsAsResponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getCurrentUserAccount(
            @PathVariable(name = "id") Long accountId
    ) throws RecordDoesNotExistException, UserIsNotOwnerException {
        return ResponseEntity.ok(accountService.findCurrentUserAccountAsResponse(accountId));
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
