package com.rainy.homebudgettracker.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/delete")
    public ResponseEntity<?> deleteUserData() {
        userService.deleteUserData();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/info")
    public ResponseEntity<UserInfoResponse> getUserInfo() {
        return ResponseEntity.ok(userService.getUserInfo());
    }
}
