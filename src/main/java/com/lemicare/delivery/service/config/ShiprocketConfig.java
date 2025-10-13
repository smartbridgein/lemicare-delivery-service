package com.lemicare.delivery.service.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Configuration
@ConfigurationProperties(prefix = "shiprocket.api")
@Getter
@Setter
@Validated
public class ShiprocketConfig {

    @NotBlank(message = "Shiprocket base URL cannot be blank")
    private String baseUrl;

    @NotBlank(message = "Shiprocket API email cannot be blank")
    @Email(message = "Shiprocket API email must be a valid email format")
    private String email;

    @NotBlank(message = "Shiprocket API password cannot be blank")
    private String password;

    @Positive(message = "Auth token cache expiry minutes must be a positive value")
    private long authTokenCacheExpiryMinutes = 55;

    @Positive(message = "WebClient response timeout seconds must be positive")
    private long webclientResponseTimeoutSeconds = 15;

    @Positive(message = "WebClient read timeout seconds must be positive")
    private long webclientReadTimeoutSeconds = 10;

    @Positive(message = "WebClient write timeout seconds must be positive")
    private long webclientWriteTimeoutSeconds = 10;

    @Min(value = 1, message = "Shiprocket token expiry minutes must be at least 1")
    private int tokenExpiryMinutes = 60; // Default to 60 minutes if not specified
}
