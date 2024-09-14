package com.rainy.homebudgettracker.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class ExchangeResponse {
    @JsonProperty("result")
    String result;

    @JsonProperty("documentation")
    String documentation;

    @JsonProperty("terms_of_use")
    String termsOfUse;

    @JsonProperty("time_last_update_unix")
    String timeLastUpdateUnix;

    @JsonProperty("time_last_update_utc")
    String timeLastUpdateUtc;

    @JsonProperty("time_next_update_unix")
    String timeNextUpdateUnix;

    @JsonProperty("time_next_update_utc")
    String timeNextUpdateUtc;

    @JsonProperty("base_code")
    String baseCode;

    @JsonProperty("target_code")
    String targetCode;

    @JsonProperty("conversion_rate")
    String conversionRate;

    @JsonProperty("error-type")
    String errorType;
}
