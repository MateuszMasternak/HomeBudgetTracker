package com.rainy.homebudgettracker.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
public class RegisterRequest {
    @NotEmpty(message = "First name is required, it can be a nickname")
    @NotBlank(message = "First name is required, it can be a nickname")
    private String firstName;
    private String lastName;
    @NotEmpty(message = "Email is required")
    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;
    @NotEmpty(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;
}
