package com.rainy.homebudgettracker.exchange;

import com.rainy.homebudgettracker.transaction.enums.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceImplTest {

    @InjectMocks
    private ExchangeServiceImpl exchangeService;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private RestClient restClient;


    @Test
    void shouldReturnExchangeRateOnSuccess() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        ExchangeResponse successResponse = ExchangeResponse.builder()
                .result("success")
                .baseCode("EUR")
                .targetCode("GBP")
                .conversionRate("0.8412")
                .build();
        ResponseEntity<ExchangeResponse> responseEntity = ResponseEntity.ok(successResponse);

        when(responseSpec.toEntity(ExchangeResponse.class)).thenReturn(responseEntity);

        ExchangeResponse result = exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.GBP);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals("success", result.getResult()),
                () -> assertEquals("EUR", result.getBaseCode()),
                () -> assertEquals("GBP", result.getTargetCode()),
                () -> assertEquals("0.8412", result.getConversionRate())
        );
    }

    @Test
    void shouldPropagateExceptionWhenApiReturns429() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", null, null, null
        );
        when(responseSpec.toEntity(ExchangeResponse.class)).thenThrow(exception);

        assertThrows(HttpClientErrorException.TooManyRequests.class, () -> {
            exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.JPY);
        });
    }

    @Test
    void shouldPropagateExceptionWhenApiReturns404() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND, "Not Found", null, null, null
        );
        when(responseSpec.toEntity(ExchangeResponse.class)).thenThrow(exception);

        assertThrows(HttpClientErrorException.NotFound.class, () -> {
            exchangeService.getExchangeRate(CurrencyCode.EUR, CurrencyCode.USD);
        });
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