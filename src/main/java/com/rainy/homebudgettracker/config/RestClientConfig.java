package com.rainy.homebudgettracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Value("${application.exchange.rate.url}")
    private String baseUrl;
    @Value("${application.exchange.rate.api-key}")
    private String apiKey;

    @Bean
    RestClient restClient() {
        String url = "%s/%s".formatted(baseUrl, apiKey);
        return RestClient.create(url);
    }
}
