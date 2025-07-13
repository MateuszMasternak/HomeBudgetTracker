package com.rainy.homebudgettracker.transaction;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalance(UUID accountId, BigDecimal balance) {
}
