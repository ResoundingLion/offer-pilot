package com.offerpilot.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyUpdateRequest {

    @NotNull
    private Long id;

    @NotNull
    private Long userId;

    @NotBlank
    private String name;

    private String industry;

    private String website;

    private String location;

    private String size;

    private String description;
}
