package com.lemicare.delivery.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryItemResponse {

    private String medicineId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double totalAmount;
}
