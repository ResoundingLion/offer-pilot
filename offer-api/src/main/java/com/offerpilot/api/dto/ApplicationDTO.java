package com.offerpilot.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 投递记录 DTO —— 供 Feign 跨服务传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDTO {
    private Long id;
    private Long userId;
    private Long companyId;
    private Long positionId;
    private String status;
    private String source;
    private LocalDateTime appliedAt;
    private String notes;
    private String pipelineConfig;
    private String currentStage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
