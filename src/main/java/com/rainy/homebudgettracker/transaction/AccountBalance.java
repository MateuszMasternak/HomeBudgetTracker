package com.rainy.homebudgettracker.transaction;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record AccountBalance(
        UUID accountId, BigDecimal balance
) { }
