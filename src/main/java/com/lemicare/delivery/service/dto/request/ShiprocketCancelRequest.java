package com.lemicare.delivery.service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiprocketCancelRequest {
    @JsonProperty("awb")
    @NotEmpty(message = "AWB list cannot be empty for cancellation")
    private List<String> awbCodes;
}