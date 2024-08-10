package com.rainy.homebudgettracker.auth;

import com.rainy.homebudgettracker.handler.exception.EmailAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.ExpiredConfirmationTokenException;
import com.rainy.homebudgettracker.handler.exception.InvalidConfirmationTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Register a new user",
            description = "Register a new user and send an activation email"
    )
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

    @Operation(
            summary = "Authenticate a user",
            description = "Authenticate a user and return a JWT token"
    )
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest authenticationRequest
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }

    @Operation(
            summary = "Activate account",
            description = "Activate an account using a token from an email"
    )
    @GetMapping("/activate-account")
    public ResponseEntity<?> activateAccount(
            @RequestParam String token
    ) throws MessagingException, InvalidConfirmationTokenException, ExpiredConfirmationTokenException {
        authenticationService.activateAccount(token);
        return ResponseEntity.accepted().build();
    }
}
