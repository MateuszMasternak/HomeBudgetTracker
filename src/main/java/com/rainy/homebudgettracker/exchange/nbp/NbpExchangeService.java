package com.rainy.homebudgettracker.exchange.nbp;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;

import java.math.BigDecimal;

public interface NbpExchangeService {
    BigDecimal getRate(CurrencyCode currency);
}
