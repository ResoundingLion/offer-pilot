package com.offerpilot.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 简历 DTO —— 供 Feign 跨服务传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDTO {
    private Long id;
    private Long userId;
    private String title;
    private Integer version;
    private String content;
    private String fileUrl;
    private String summary;
    private String contentText;
    private Boolean isCurrent;
}
