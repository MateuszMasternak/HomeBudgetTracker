package com.rainy.homebudgettracker.exchange;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final RestClient restClient;

    public ExchangeResponse getExchangeRate(String baseCurrency, String targetCurrency) {
        String url = String.format("/pair/%s/%s", baseCurrency, targetCurrency);
        ResponseEntity<ExchangeResponse> response = restClient.get()
                .uri(url)
                .retrieve()
                .toEntity(ExchangeResponse.class);
        return response.getBody();
    }
}
