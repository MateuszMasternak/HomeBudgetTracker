package com.rainy.homebudgettracker.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class SumResponse {
    @NotNull(message = "Amount is required.")
    private String amount;
}
