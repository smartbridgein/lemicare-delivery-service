package com.lemicare.delivery.service.dto.response;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String id;

    private String orderId;

    private String organizationId;

    private String branchId;

    private String customerId;

    private String partnerName;


    private String status;

    private Date createdAt;

    private Date updatedAt;

    private Date deliveredAt;

    private int courierId;

    private int shipmentId;
}
