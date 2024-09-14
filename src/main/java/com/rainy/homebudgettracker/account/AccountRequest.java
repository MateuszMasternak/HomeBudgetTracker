package com.rainy.homebudgettracker.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccountRequest {
    @NotEmpty(message = "Name is required")
    @NotBlank(message = "Name is required")
    private String name;
    @NotEmpty(message = "Currency code is required")
    @NotBlank(message = "Currency code is required")
    private String currencyCode;
}
