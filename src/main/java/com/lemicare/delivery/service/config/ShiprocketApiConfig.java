package com.lemicare.delivery.service.config;

import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class ShiprocketApiConfig {

    @Value("${shiprocket.api.base-url}")
    private String baseUrl;

    @Bean
    public WebClient shiprocketWebClient() {
        // --- ADD THIS HTTP CLIENT CONFIGURATION FOR DETAILED LOGGING ---
        HttpClient httpClient = HttpClient.create()
                .wiretap("reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                // --- MIMIC POSTMAN'S USER-AGENT HEADER (VERY IMPORTANT) ---
                .defaultHeader(HttpHeaders.USER_AGENT, "PostmanRuntime/7.39.0") // Use a recent Postman version
                .clientConnector(new ReactorClientHttpConnector(httpClient)) // Apply the logging client
                .filters(exchangeFilterFunctions -> { // Apply filters for readable logging
                    exchangeFilterFunctions.add(logRequest());
                    exchangeFilterFunctions.add(logResponse());
                })
                .build();
    }

    // --- ADD THESE HELPER METHODS FOR LOGGING ---
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info(">>> SHIPROCKET REQUEST: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info(">>> HEADER: {}: {}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("<<< SHIPROCKET RESPONSE: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
