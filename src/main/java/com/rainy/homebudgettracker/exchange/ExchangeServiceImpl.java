package com.rainy.homebudgettracker.exchange;

import com.rainy.homebudgettracker.handler.exception.ExchangeRateApiException;
import com.rainy.homebudgettracker.handler.exception.QuotaReachedException;
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
        ExchangeResponse exchangeResponse = getExchangeResponse(baseCurrency, targetCurrency);

        if (exchangeResponse == null) {
            throw new ExchangeRateApiException("Failed to get exchange rate.");
        } else if ("error".equals(exchangeResponse.getResult())) {
            if ("quota-reached".equals(exchangeResponse.getErrorType())) {
                throw new QuotaReachedException("Quota reached. Provide a custom rate.");
            } else {
                throw new ExchangeRateApiException("Failed to get exchange rate.");
            }
        }

        return exchangeResponse;
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
