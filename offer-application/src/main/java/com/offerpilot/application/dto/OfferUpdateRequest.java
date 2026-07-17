package com.offerpilot.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OfferUpdateRequest {

    @NotNull
    private Long id;

    private String salary;

    private String bonus;

    private String stock;

    private String benefits;

    private LocalDate deadline;

    private String remark;
}
