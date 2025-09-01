package com.lemicare.delivery.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShiprocketAuthRequest {
    private String email;
    private String password;
}
