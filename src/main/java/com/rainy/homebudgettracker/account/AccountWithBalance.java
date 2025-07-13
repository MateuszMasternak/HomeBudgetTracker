package com.rainy.homebudgettracker.account;

import java.math.BigDecimal;

public record AccountWithBalance(
   Account account,
    BigDecimal balance
) {}
