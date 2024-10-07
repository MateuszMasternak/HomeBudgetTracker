package com.rainy.homebudgettracker.exchange;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;

public interface ExchangeService {
    ExchangeResponse getExchangeRate(CurrencyCode baseCurrency, CurrencyCode targetCurrency);
}
