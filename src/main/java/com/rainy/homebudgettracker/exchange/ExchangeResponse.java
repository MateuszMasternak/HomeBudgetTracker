package com.rainy.homebudgettracker.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


@Builder
public record ExchangeResponse (
    @JsonProperty("result") String result,
    @JsonProperty("base_code") String baseCode,
    @JsonProperty("target_code") String targetCode,
    @JsonProperty("conversion_rate") String conversionRate,
    @JsonProperty("error-type") String errorType
) {}
