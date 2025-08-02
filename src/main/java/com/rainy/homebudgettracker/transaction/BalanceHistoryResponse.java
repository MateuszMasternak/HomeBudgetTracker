package com.rainy.homebudgettracker.transaction;

import java.math.BigDecimal;
import java.util.List;

public record BalanceHistoryResponse(
        BigDecimal initialBalance,
        List<BigDecimal> periodicBalance
) {}

