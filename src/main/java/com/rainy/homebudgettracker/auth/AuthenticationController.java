package com.rainy.homebudgettracker.auth;

import com.rainy.homebudgettracker.handler.exception.EmailAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.EmailAlreadyInUseException;
import com.rainy.homebudgettracker.handler.exception.ExpiredConfirmationTokenException;
import com.rainy.homebudgettracker.handler.exception.InvalidConfirmationTokenException;
import com.rainy.homebudgettracker.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegisterRequest registerRequest
    ) throws MessagingException {
        String message = "You will receive an email with an activation code soon";
        try {
            authenticationService.register(registerRequest);
            return ResponseEntity.accepted().body(message);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.accepted().body(message);
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest authenticationRequest
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }

    @GetMapping("/activate-account")
    public ResponseEntity<?> activateAccount(
            @RequestParam String token
    ) throws MessagingException, InvalidConfirmationTokenException, ExpiredConfirmationTokenException {
        authenticationService.activateAccount(token);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/password-reset-link")
    public ResponseEntity<?> resetPassword(
            @RequestBody @Valid PasswordResetLinkRequest email
    ) throws MessagingException, UsernameNotFoundException {
        String message = "You will receive an email with a password reset link soon";
        try {
            authenticationService.sendPasswordResetEmail(email);
            return ResponseEntity.accepted().body(message);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.accepted().body(message);
        }
    }

    @PostMapping("/password-reset")
    public ResponseEntity<?> changePassword(
            @RequestParam String token,
            @RequestBody @Valid ChangePasswordRequest password
    ) throws InvalidConfirmationTokenException, ExpiredConfirmationTokenException {
        authenticationService.changePassword(token, password);
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid ChangePasswordRequest password
    ) {
        authenticationService.changePassword(password);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-email")
    public ResponseEntity<?> changeEmail(
            @RequestBody @Valid ChangeEmailRequest email
    ) throws EmailAlreadyInUseException {
        authenticationService.changeEmail(email);
        return ResponseEntity.ok().build();
    }
}
