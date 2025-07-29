package com.rainy.homebudgettracker.exchange;

import com.rainy.homebudgettracker.exchange.nbp.NbpExchangeService;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class ExchangeServiceImpl implements ExchangeService {
    private final RestClient exchangeRateApiRestClient;
    private final NbpExchangeService nbpExchangeService;

    public ExchangeServiceImpl(@Qualifier("exchangeRateApi") RestClient exchangeRateApiRestClient, NbpExchangeService nbpExchangeService) {
        this.exchangeRateApiRestClient = exchangeRateApiRestClient;
        this.nbpExchangeService = nbpExchangeService;
    }

    @Cacheable(
            value = "exchangeRates",
            key = "#baseCurrency.name() + '-' + #targetCurrency.name() + '-' + {T(java.time.LocalDate).now().toString()}"
    )
    @Override
    public ExchangeResponse getExchangeRate(CurrencyCode baseCurrency, CurrencyCode targetCurrency) {
        if (baseCurrency == null || targetCurrency == null) {
            throw new IllegalArgumentException("Base and target currencies cannot be null");
        }

        if (baseCurrency.equals(targetCurrency)) {
            return ExchangeResponse.builder()
                    .result("success")
                    .baseCode(baseCurrency.name())
                    .targetCode(targetCurrency.name())
                    .conversionRate("1.0")
                    .build();
        }

        try {
            return getFromExchangeRateApi(baseCurrency, targetCurrency);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                System.out.println("Quota reached on primary API, falling back to NBP.");
                return getFromNbpApi(baseCurrency, targetCurrency);
            }

            throw ex;
        }
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

    private ExchangeResponse getFromExchangeRateApi(CurrencyCode baseCurrency, CurrencyCode targetCurrency) {
        String url = String.format("/pair/%s/%s", baseCurrency.name(), targetCurrency.name());
        ResponseEntity<ExchangeResponse> response = exchangeRateApiRestClient.get()
                .uri(url)
                .retrieve()
                .toEntity(ExchangeResponse.class);

        if (response.getBody() == null) {
            throw new IllegalStateException("Primary API response body is null.");
        }
        return response.getBody();
    }

    private ExchangeResponse getFromNbpApi(CurrencyCode baseCurrency, CurrencyCode targetCurrency) {
        BigDecimal baseRateInPln = nbpExchangeService.getRate(baseCurrency);
        BigDecimal targetRateInPln = nbpExchangeService.getRate(targetCurrency);

        BigDecimal conversionRate = baseRateInPln.divide(targetRateInPln, 4, RoundingMode.HALF_UP);

        return ExchangeResponse.builder()
                .result("success")
                .baseCode(baseCurrency.name())
                .targetCode(targetCurrency.name())
                .conversionRate(conversionRate.toPlainString())
                .build();
    }
}
