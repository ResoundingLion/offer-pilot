package com.offerpilot.application.vo;

import com.offerpilot.application.enums.ApplicationSource;
import com.offerpilot.application.enums.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationVO {
    private Long id;
    private Long userId;
    private Long companyId;
    private Long positionId;
    private ApplicationStatus status;
    private ApplicationSource source;
    private LocalDateTime appliedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
