package com.lemicare.delivery.service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShiprocketAuthResponse {

    /**
     * The user ID from Shiprocket.
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * The first name of the authenticated user.
     */
    @JsonProperty("first_name")
    private String firstName;

    /**
     * The last name of the authenticated user.
     */
    @JsonProperty("last_name")
    private String lastName;

    /**
     * The email of the authenticated user.
     */
    @JsonProperty("email")
    private String email;

    /**
     * The company ID associated with the user in Shiprocket.
     */
    @JsonProperty("company_id")
    private Integer companyId;

    /**
     * The timestamp when the user was created.
     * Mapped as a String for simplicity and robustness.
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * The critical bearer token required for all subsequent authenticated API calls.
     * This is the main piece of information we need from this response.
     */
    @JsonProperty("token")
    private String token;

}

