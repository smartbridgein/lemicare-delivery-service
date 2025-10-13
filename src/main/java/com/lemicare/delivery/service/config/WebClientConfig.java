package com.lemicare.delivery.service.config;

import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.time.Duration;
import java.util.concurrent.TimeUnit; // Import for TimeUnit

@Configuration
@Slf4j
public class WebClientConfig {

    private final ShiprocketConfig shiprocketConfig;

    public WebClientConfig(ShiprocketConfig shiprocketConfig) {
        this.shiprocketConfig = shiprocketConfig;
    }

    @Bean
    public WebClient.Builder shiprocketWebClientBuilder() {
        // Configure HttpClient with timeouts and detailed wiretap logging
        HttpClient httpClient = HttpClient.create()
                // Connection/Response timeout for the entire request/response cycle
                .responseTimeout(Duration.ofSeconds(shiprocketConfig.getWebclientResponseTimeoutSeconds()))
                .doOnConnected(conn -> {
                    // Read timeout for periods of no data being received
                    conn.addHandlerLast(new ReadTimeoutHandler(shiprocketConfig.getWebclientReadTimeoutSeconds(), TimeUnit.SECONDS));
                    // Write timeout for periods of no data being sent
                    conn.addHandlerLast(new WriteTimeoutHandler(shiprocketConfig.getWebclientWriteTimeoutSeconds(), TimeUnit.SECONDS));
                })
                // Detailed wiretap logging for network traffic
                .wiretap("reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "LemicaDeliveryService/1.0 (Shiprocket Integration)") // Custom User-Agent
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(logRequest());
                    exchangeFilterFunctions.add(logResponse());
                });
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug(">>> SHIPROCKET REQUEST: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> {
                if (!name.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                    values.forEach(value -> log.debug(">>> HEADER: {}: {}", name, value));
                } else {
                    log.debug(">>> HEADER: {}: [PROTECTED]", name);
                }
            });
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("<<< SHIPROCKET RESPONSE: Status {}", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) ->
                    values.forEach(value -> log.debug("<<< HEADER: {}: {}", name, value)));
            return Mono.just(clientResponse);
        });
    }
}
