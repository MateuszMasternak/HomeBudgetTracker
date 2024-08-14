package com.rainy.homebudgettracker.auth;

import com.rainy.homebudgettracker.handler.exception.EmailAlreadyExistsException;
import com.rainy.homebudgettracker.handler.exception.ExpiredConfirmationTokenException;
import com.rainy.homebudgettracker.handler.exception.InvalidConfirmationTokenException;
import com.rainy.homebudgettracker.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(
            summary = "Register a new user",
            description = "Register a new user and send an activation email",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Accepted",
                            content = @Content(
                                    schema = @Schema(
                                            example = "You will receive an email with an activation code soon"
                                    ),
                                    mediaType = "text/plain"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request",
                            content = @Content(
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 306,
                                                        "businessErrorDescription": "Missing or invalid request body element",
                                                        "validationErrors": [
                                                            "Email is not valid"
                                                        ]
                                                    }"""
                                    ),
                                    mediaType = "application/json"

                            )
                    )
            }
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
            description = "Authenticate a user and return a JWT token",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = @Content(
                                    schema = @Schema(
                                            implementation = AuthenticationResponse.class
                                    ),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request",
                            content = @Content(
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 306,
                                                        "businessErrorDescription": "Missing or invalid request body element",
                                                        "validationErrors": [
                                                            "Email is not valid"
                                                        ]
                                                    }"""
                                    ),
                                    mediaType = "application/json"

                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 301,
                                                        "businessErrorDescription": "Bad credentials"
                                                    }"""
                                    ),
                                    mediaType = "application/json"

                            )
                    )
            }
    )
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest authenticationRequest
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }

    @Operation(
            summary = "Activate account",
            description = "Activate an account using a token from an email",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Accepted",
                            content = @Content(
                                    schema = @Schema(),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 304,
                                                        "businessErrorDescription": "Invalid confirmation token"
                                                    }"""
                                    ),
                                    mediaType = "application/json"

                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 402,
                                                        "businessErrorDescription": "Record does not exist or is not accessible"
                                                    }"""
                                    )
                            )
                    ),
            }
    )
    @GetMapping("/activate-account")
    public ResponseEntity<?> activateAccount(
            @RequestParam String token
    ) throws MessagingException, InvalidConfirmationTokenException, ExpiredConfirmationTokenException {
        authenticationService.activateAccount(token);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Reset password",
            description = "Reset password and send an email with a reset link",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Accepted",
                            content = @Content(
                                    schema = @Schema(
                                            example = "You will receive an email with a password reset link soon"
                                    ),
                                    mediaType = "text/plain"
                            )
                    )
            }
    )
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

    @Operation(
            summary = "Change password",
            description = "Change password using a token from an email",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Accepted",
                            content = @Content(
                                    schema = @Schema(),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 304,
                                                        "businessErrorDescription": "Invalid confirmation token"
                                                    }"""
                                    ),
                                    mediaType = "application/json"

                            )
                    )
            }
    )
    @PostMapping("/password-reset")
    public ResponseEntity<?> changePassword(
            @RequestParam String token,
            @RequestBody @Valid ChangePasswordRequest password
    ) throws InvalidConfirmationTokenException, ExpiredConfirmationTokenException {
        authenticationService.changePassword(token, password);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Change password",
            description = "Change password for authenticated user",
            responses = {
                    @ApiResponse(
                            responseCode = "202",
                            description = "Accepted",
                            content = @Content(
                                    schema = @Schema(),
                                    mediaType = "application/json"
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request",
                            content = @Content(
                                    schema = @Schema(
                                            example = """
                                                    {
                                                        "businessErrorCode": 306,
                                                        "businessErrorDescription": "Missing or invalid request body element",
                                                        "validationErrors": [
                                                            "Password is not valid"
                                                        ]
                                                    }"""
                                    ),
                                    mediaType = "application/json"

                            )
                    )
            }
    )
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid ChangePasswordRequest password
    ) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authenticationService.changePassword(user, password);
        return ResponseEntity.accepted().build();
    }
}
