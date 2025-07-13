package com.rainy.homebudgettracker.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

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
            @PathVariable(name = "id") UUID accountId
    ) {
        return ResponseEntity.ok(accountService.findCurrentUserAccountAsResponse(accountId));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccountForCurrentUser(
            @RequestBody @Valid AccountRequest request) {
        AccountResponse accountResponse = accountService.createAccountForCurrentUser(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(accountResponse.getId())
                .toUri();

        return ResponseEntity.created(location).body(accountResponse);
    }

    @PatchMapping
    public ResponseEntity<AccountResponse> updateCurrentUserAccountName(
            @RequestBody @Valid AccountUpdateNameRequest request
    ) {
        return ResponseEntity.ok(accountService.updateCurrentUserAccountName(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrentUserAccount(
            @PathVariable(name = "id") UUID accountId
    ) {
        accountService.deleteCurrentUserAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}
