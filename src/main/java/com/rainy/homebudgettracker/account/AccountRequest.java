package com.rainy.homebudgettracker.account;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "Currency code is required")
    private CurrencyCode currencyCode;
}
