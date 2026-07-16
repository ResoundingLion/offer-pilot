package com.offerpilot.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PositionUpdateRequest {

    @NotNull
    private Long id;

    @NotNull
    private Long companyId;

    @NotBlank
    private String title;

    private Integer salaryMin;

    private Integer salaryMax;

    private String city;

    private String education;

    private String experience;

    private String employmentType;

    private String description;

    private LocalDate deadline;
}
