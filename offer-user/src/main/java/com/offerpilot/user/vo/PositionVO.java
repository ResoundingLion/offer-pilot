package com.offerpilot.user.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PositionVO {
    private Long id;
    private Long companyId;
    private String title;
    private Integer salaryMin;
    private Integer salaryMax;
    private String city;
    private String education;
    private String experience;
    private String employmentType;
    private String description;
    private Integer status;
    private LocalDate deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
