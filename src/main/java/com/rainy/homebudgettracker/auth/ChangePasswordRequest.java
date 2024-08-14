package com.rainy.homebudgettracker.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {
    @NotEmpty(message = "Password is required")
    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@|$!%*?&^+=<>_()/:;'\"`\\\\~-])[A-Za-z\\d@|$!%*?&^+=<>_()/:;'\"`\\\\~-]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character: @|$!%*?&^+=<>_()/:;'\"`\\~-")
    private String password;
}