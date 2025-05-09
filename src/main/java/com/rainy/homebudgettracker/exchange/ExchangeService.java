package com.rainy.homebudgettracker.exchange;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;

import java.time.LocalDate;

public interface ExchangeService {
    ExchangeResponse getExchangeRate(CurrencyCode baseCurrency, CurrencyCode targetCurrency);
    ExchangeResponse getHistoricalExchangeRate(CurrencyCode baseCurrency, CurrencyCode targetCurrency, LocalDate date);
}
