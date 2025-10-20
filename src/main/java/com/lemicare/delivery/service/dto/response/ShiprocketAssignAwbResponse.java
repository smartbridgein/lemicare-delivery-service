package com.lemicare.delivery.service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiprocketAssignAwbResponse {
    private boolean status;
    private String message;
    private AssignAwbData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssignAwbData {
        private Long shipment_id;
        private String awb_code;
        private Integer courier_id;
        private String courier_name;
        private String status;
    }
}
