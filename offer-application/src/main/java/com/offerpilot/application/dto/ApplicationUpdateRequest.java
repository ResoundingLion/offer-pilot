package com.offerpilot.application.dto;

import com.offerpilot.application.enums.ApplicationSource;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationUpdateRequest {

    @NotNull
    private Long id;

    @NotNull
    private Long companyId;

    @NotNull
    private Long positionId;

    private ApplicationSource source;

    private LocalDateTime appliedAt;

    private String notes;
}
