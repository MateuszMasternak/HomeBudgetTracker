package com.rainy.homebudgettracker.exchange;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {
    private final RestClient restClient;

    @Override
    public ExchangeResponse getExchangeRate(CurrencyCode baseCurrency, CurrencyCode targetCurrency) {
        if (baseCurrency == null || targetCurrency == null) {
            throw new IllegalArgumentException("Base and target currencies cannot be null");
        }
        // Exceptions are handled by the RestClient
        // 4xxx and 5xx responses will throw an exception which is handled by the global exception handler
        return getExchangeResponse(baseCurrency, targetCurrency);
    }

    @Override
    public ExchangeResponse getHistoricalExchangeRate(
            CurrencyCode baseCurrency, CurrencyCode targetCurrency, LocalDate date
    ) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        // TODO: Find out how to get historical exchange rates - exchangerate-api.com does not support this in the free plan
        return getExchangeRate(baseCurrency, targetCurrency);
    }

    private ExchangeResponse getExchangeResponse(CurrencyCode baseCurrency, CurrencyCode targetCurrency) {
        String url = String.format("/pair/%s/%s", baseCurrency.name(), targetCurrency.name());
        ResponseEntity<ExchangeResponse> response = restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(ExchangeResponse.class);

        return response.getBody();
    }
}
