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
    private String companyName;
    private Long positionId;
    private String positionTitle;
    private ApplicationStatus status;
    private ApplicationSource source;
    private LocalDateTime appliedAt;
    private String notes;
    private String pipelineConfig;
    private String currentStage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
