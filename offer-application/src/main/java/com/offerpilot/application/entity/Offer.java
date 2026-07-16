package com.offerpilot.application.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.offerpilot.application.enums.OfferStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Offer 记录 —— 关联投递，一次投递至多一个 Offer（UK 约束）
 */
@Data
@TableName("offer")
public class Offer {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联投递 ID（UNIQUE）
     */
    private Long applicationId;

    /**
     * 薪资描述
     */
    private String salary;

    /**
     * 奖金/期权
     */
    private String bonus;

    /**
     * 股票
     */
    private String stock;

    /**
     * 福利
     */
    private String benefits;

    /**
     * Offer 有效期截止日
     */
    private LocalDate deadline;

    /**
     * Offer 状态（待接受/已接受/已拒绝）
     */
    private OfferStatus status;

    /**
     * 备注
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
