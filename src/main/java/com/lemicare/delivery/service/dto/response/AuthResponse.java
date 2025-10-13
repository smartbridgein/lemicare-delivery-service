package com.lemicare.delivery.service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public  class AuthResponse {
    private String token;
    @JsonProperty("token_type")
    private String tokenType;
    private String message;
}
