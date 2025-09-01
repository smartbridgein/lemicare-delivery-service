package com.lemicare.delivery.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class ShiprocketCancelRequest {
    // According to some versions of Shiprocket docs, they expect a list of AWBs.
    private List<String> awbs;
}
