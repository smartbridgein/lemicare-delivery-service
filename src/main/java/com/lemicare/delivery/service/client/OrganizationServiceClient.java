package com.lemicare.delivery.service.client;

import com.lemicare.delivery.service.client.dto.BranchConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * A declarative Feign client for communicating with the organization-service.
 */
@FeignClient(name = "organization-service", url = "${services.organization.url}")
public interface OrganizationServiceClient {

    /**
     * Fetches the branch-specific configuration, including the Shiprocket pickup location.
     */
    @GetMapping("/api/v1/internal/organizations/{orgId}/branches/{branchId}/config")
    BranchConfig getBranchConfig(
            @PathVariable("orgId") String organizationId,
            @PathVariable("branchId") String branchId
    );
}
