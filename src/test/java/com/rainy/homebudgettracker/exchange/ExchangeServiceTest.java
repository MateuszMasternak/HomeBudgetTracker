package com.rainy.homebudgettracker.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rainy.homebudgettracker.handler.exception.ExchangeRateApiException;
import com.rainy.homebudgettracker.handler.exception.QuotaReachedException;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExchangeServiceTest {
    private ExchangeService exchangeService;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        var restClient = mock(RestClient.class, RETURNS_DEEP_STUBS);

        ResponseEntity<ExchangeResponse> exchangeResponse = ResponseEntity.ok(ExchangeResponse.builder()
                .result("success")
                .documentation("https://www.exchangerate-api.com/docs")
                .termsOfUse("https://www.exchangerate-api.com/terms")
                .timeLastUpdateUnix("1585267200")
                .timeLastUpdateUtc("Fri, 27 Mar 2020 00:00:00 +0000")
                .timeNextUpdateUnix("1585270800")
                .timeNextUpdateUtc("Sat, 28 Mar 2020 01:00:00 +0000")
                .baseCode("EUR")
                .targetCode("GBP")
                .conversionRate("0.8412")
                .build());

        ResponseEntity<ExchangeResponse> exchangeResponseQuotaReached = ResponseEntity.ok(ExchangeResponse.builder()
                .result("error")
                .errorType("quota-reached")
                .build());

        ResponseEntity<ExchangeResponse> exchangeResponseError = ResponseEntity.ok(ExchangeResponse.builder()
                .result("error")
                .errorType("other")
                .build());

        ResponseEntity<ExchangeResponse> exchangeResponseNull = ResponseEntity.ok(null);

        exchangeService = new ExchangeServiceImpl(restClient);

        when(restClient.get().uri("/pair/EUR/GBP").retrieve().toEntity(ExchangeResponse.class))
                .thenReturn(exchangeResponse);
        when(restClient.get().uri("/pair/EUR/USD").retrieve().toEntity(ExchangeResponse.class))
                .thenReturn(exchangeResponseError);
        when(restClient.get().uri("/pair/EUR/JPY").retrieve().toEntity(ExchangeResponse.class))
                .thenReturn(exchangeResponseQuotaReached);
        when(restClient.get().uri("/pair/USD/JPY").retrieve().toEntity(ExchangeResponse.class))
                .thenReturn(exchangeResponseNull);
    }

    @Test
    void getExchangeRate() {
        ExchangeResponse exchangeResponse = exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.GBP);
        assertEquals("success", exchangeResponse.getResult());
        assertEquals("https://www.exchangerate-api.com/docs", exchangeResponse.getDocumentation());
        assertEquals("https://www.exchangerate-api.com/terms", exchangeResponse.getTermsOfUse());
        assertEquals("1585267200", exchangeResponse.getTimeLastUpdateUnix());
        assertEquals("Fri, 27 Mar 2020 00:00:00 +0000", exchangeResponse.getTimeLastUpdateUtc());
        assertEquals("1585270800", exchangeResponse.getTimeNextUpdateUnix());
        assertEquals("Sat, 28 Mar 2020 01:00:00 +0000", exchangeResponse.getTimeNextUpdateUtc());
        assertEquals("EUR", exchangeResponse.getBaseCode());
        assertEquals("GBP", exchangeResponse.getTargetCode());
        assertEquals("0.8412", exchangeResponse.getConversionRate());
    }

    @Test
    void getExchangeRateQuotaReached() {
        assertThrows(QuotaReachedException.class,
                () -> exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.JPY));
    }

    @Test
    void getExchangeRateError() {
        assertThrows(ExchangeRateApiException.class,
                () -> exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.USD));
    }

    @Test
    void getExchangeRateNull() {
        assertThrows(ExchangeRateApiException.class,
                () -> exchangeService.getExchangeRate(CurrencyCode.USD, CurrencyCode.JPY));
    }
}