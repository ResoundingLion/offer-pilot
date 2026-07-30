package com.offerpilot.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 面试记录 DTO —— 供 Feign 跨服务传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewDTO {
    private Long id;
    private Long applicationId;
    private String round;
    private LocalDateTime scheduledAt;
    private String interviewType;
    private String location;
    private String interviewer;
    private String result;
    private String feedback;
}
