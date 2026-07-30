package com.offerpilot.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Offer DTO —— 供 Feign 跨服务传输
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferDTO {
    private Long id;
    private Long applicationId;
    private String salary;
    private String bonus;
    private String stock;
    private String benefits;
    private LocalDate deadline;
    private String status;
    private String remark;
}
