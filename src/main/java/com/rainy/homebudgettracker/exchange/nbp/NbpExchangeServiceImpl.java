package com.rainy.homebudgettracker.exchange.nbp;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class NbpExchangeServiceImpl implements NbpExchangeService {
    private final RestClient nbpRestClient;

    public NbpExchangeServiceImpl(@Qualifier("nbpApi") RestClient nbpRestClient) {
        this.nbpRestClient = nbpRestClient;
    }

    @Override
    public BigDecimal getRate(CurrencyCode currency) {
        if (currency == CurrencyCode.PLN) {
            return BigDecimal.ONE;
        }

        String url = String.format("/exchangerates/rates/A/%s/", currency.name());

        NbpExchangeRateResponse response = nbpRestClient.get()
                .uri(url)
                .retrieve()
                .body(NbpExchangeRateResponse.class);

        return Optional.ofNullable(response)
                .map(NbpExchangeRateResponse::rates)
                .filter(rates -> !rates.isEmpty())
                .map(rates -> rates.get(0).mid())
                .orElseThrow(() -> new IllegalStateException("Could not retrieve rate from NBP for currency: " + currency));
    }
}
