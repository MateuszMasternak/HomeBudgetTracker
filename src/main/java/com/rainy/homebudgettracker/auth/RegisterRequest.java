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
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
