package com.rainy.homebudgettracker.exchange;

import com.rainy.homebudgettracker.exchange.nbp.NbpExchangeService;
import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @InjectMocks
    private ExchangeServiceImpl exchangeService;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;
    @Mock
    @Qualifier("exchangeRateApi")
    private RestClient exchangeRateApiRestClient;

    @Mock
    private NbpExchangeService nbpExchangeService;

    private void mockRestClientChain() {
        when(exchangeRateApiRestClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void shouldReturnExchangeRateOnSuccessFromPrimaryApi() {
        mockRestClientChain();
        ExchangeResponse successResponse = ExchangeResponse.builder()
                .result("success").baseCode("EUR").targetCode("GBP").conversionRate("0.8412").build();
        ResponseEntity<ExchangeResponse> responseEntity = ResponseEntity.ok(successResponse);
        when(responseSpec.toEntity(ExchangeResponse.class)).thenReturn(responseEntity);

        ExchangeResponse result = exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.GBP);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("success", result.result()),
                () -> assertEquals("0.8412", result.conversionRate())
        );
        verify(nbpExchangeService, never()).getRate(any());
    }

    @Test
    void shouldReturnExchangeRateFromNbpOnQuotaReached() {
        mockRestClientChain();
        HttpClientErrorException quotaException = HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", null, null, null
        );
        when(responseSpec.toEntity(ExchangeResponse.class)).thenThrow(quotaException);

        when(nbpExchangeService.getRate(CurrencyCode.EUR)).thenReturn(new BigDecimal("4.30"));
        when(nbpExchangeService.getRate(CurrencyCode.USD)).thenReturn(new BigDecimal("4.00"));

        ExchangeResponse result = exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.USD);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("success", result.result()),
                () -> assertEquals("EUR", result.baseCode()),
                () -> assertEquals("USD", result.targetCode()),
                () -> assertEquals("1.0750", result.conversionRate())
        );
        verify(nbpExchangeService, times(1)).getRate(CurrencyCode.EUR);
        verify(nbpExchangeService, times(1)).getRate(CurrencyCode.USD);
    }

    @Test
    void shouldReturnRateOfOneForSameCurrencies() {
        ExchangeResponse result = exchangeService.getExchangeRate(CurrencyCode.USD, CurrencyCode.USD);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("success", result.result()),
                () -> assertEquals("USD", result.baseCode()),
                () -> assertEquals("USD", result.targetCode()),
                () -> assertEquals("1.0", result.conversionRate())
        );
        verify(exchangeRateApiRestClient, never()).get();
        verify(nbpExchangeService, never()).getRate(any());
    }

    @Test
    void shouldPropagateExceptionWhenPrimaryApiReturnsOtherError() {
        mockRestClientChain();
        HttpClientErrorException notFoundException = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", null, null, null
        );
        when(responseSpec.toEntity(ExchangeResponse.class)).thenThrow(notFoundException);

        assertThrows(HttpClientErrorException.NotFound.class, () -> {
            exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.USD);
        });
        verify(nbpExchangeService, never()).getRate(any());
    }

    @ParameterizedTest
    @MethodSource("provideNullCurrencyArguments")
    void shouldThrowIllegalArgumentExceptionWhenAnyCurrencyIsNull(CurrencyCode baseCurrency, CurrencyCode targetCurrency) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            exchangeService.getExchangeRate(baseCurrency, targetCurrency);
        });
        assertEquals("Base and target currencies cannot be null", exception.getMessage());
    }

    private static Stream<Arguments> provideNullCurrencyArguments() {
        return Stream.of(
                Arguments.of(null, CurrencyCode.USD),
                Arguments.of(CurrencyCode.EUR, null),
                Arguments.of(null, null)
        );
    }
}
