package com.offerpilot.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 状态更新请求体 —— 只包含一个状态字段
 * 用于 PATCH /api/applications/{id}/status
 */
@Data
public class StatusUpdateRequest {

    @NotBlank(message = "状态不能为空")
    private String status;
}
