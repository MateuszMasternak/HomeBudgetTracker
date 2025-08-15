package com.rainy.homebudgettracker.transaction.dto;

import java.math.BigDecimal;
import java.util.List;

public record BalanceHistoryResponse(
        BigDecimal initialBalance,
        List<BigDecimal> periodicBalance
) {}

