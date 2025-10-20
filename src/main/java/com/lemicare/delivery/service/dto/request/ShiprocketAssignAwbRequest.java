package com.lemicare.delivery.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiprocketAssignAwbRequest {
    private Long shipment_id;
    private Integer courier_id;
}
