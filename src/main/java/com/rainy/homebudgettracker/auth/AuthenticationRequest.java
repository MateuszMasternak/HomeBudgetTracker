package com.rainy.homebudgettracker.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
public class AuthenticationRequest {
    @NotEmpty(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;
    @NotEmpty(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@|$!%*?&^+=<>_()/:;'\"`\\\\~-])[A-Za-z\\d@|$!%*?&^+=<>_()/:;'\"`\\\\~-]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character: @|$!%*?&^+=<>_()/:;'\"`\\~-")
    private String password;
}
