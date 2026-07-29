package com.offerpilot.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResumeVO {
    private Long id;
    private Long userId;
    private String title;
    private Integer version;
    private String content;
    private String fileUrl;
    private String summary;
    private Boolean isCurrent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
