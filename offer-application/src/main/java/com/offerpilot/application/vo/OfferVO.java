package com.offerpilot.application.vo;

import com.offerpilot.application.enums.OfferStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OfferVO {
    private Long id;
    private Long applicationId;
    private String salary;
    private String bonus;
    private String stock;
    private String benefits;
    private LocalDate deadline;
    private OfferStatus status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
