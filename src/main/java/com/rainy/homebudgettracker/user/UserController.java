package com.rainy.homebudgettracker.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUserData() {
        userService.deleteCognitoUser();
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/info")
    public ResponseEntity<UserInfoResponse> getUserInfo() {
        return ResponseEntity.ok(userService.getUserInfo());
    }

    @GetMapping("/default-currency")
    public ResponseEntity<DefaultCurrencyResponseRequest> getDefaultCurrency() {
        return ResponseEntity.ok(userService.getDefaultCurrency());
    }

    @PostMapping("/default-currency")
    public ResponseEntity<DefaultCurrencyResponseRequest> setDefaultCurrency(
            @RequestBody DefaultCurrencyResponseRequest request) {
        return ResponseEntity.ok(userService.setDefaultCurrency(request));
    }
}
