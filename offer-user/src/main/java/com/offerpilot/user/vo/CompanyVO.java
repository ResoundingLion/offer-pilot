package com.offerpilot.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompanyVO {
    private Long id;
    private Long userId;
    private String name;
    private String industry;
    private String website;
    private String location;
    private String size;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
