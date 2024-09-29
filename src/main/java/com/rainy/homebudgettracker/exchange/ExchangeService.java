package com.rainy.homebudgettracker.exchange;

import com.rainy.homebudgettracker.handler.exception.ExchangeRateApiException;
import com.rainy.homebudgettracker.handler.exception.QuotaReachedException;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final RestClient restClient;

    public ExchangeResponse getExchangeRate(CurrencyCode baseCurrency, CurrencyCode targetCurrency) {
        String url = String.format("/pair/%s/%s", baseCurrency.name(), targetCurrency.name());
        ResponseEntity<ExchangeResponse> response = restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(ExchangeResponse.class);

        ExchangeResponse exchangeResponse = response.getBody();
        if (exchangeResponse == null) {
            throw new ExchangeRateApiException("Failed to get exchange rate.");
        } else if ("error".equals(exchangeResponse.getResult())) {
            if ("quota-reached".equals(exchangeResponse.getErrorType())) {
                throw new QuotaReachedException("Quota reached. Provide a custom rate.");
            }
        } else if (!"success".equals(exchangeResponse.getResult())) {
            throw new ExchangeRateApiException("Failed to get exchange rate.");
        }

        return response.getBody();
    }
}
