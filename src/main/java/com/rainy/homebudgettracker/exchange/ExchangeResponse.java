package com.rainy.homebudgettracker.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExchangeResponse {
    @JsonProperty("result")
    String result;

    @JsonProperty("base_code")
    String baseCode;

    @JsonProperty("target_code")
    String targetCode;

    @JsonProperty("conversion_rate")
    String conversionRate;

    @JsonProperty("error-type")
    String errorType;
}
