package com.offerpilot.application.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.offerpilot.application.enums.ApplicationSource;
import com.offerpilot.application.enums.ApplicationStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递记录 —— 核心业务表
 */
@Data
@TableName("application")
public class Application {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 投递者
     */
    private Long userId;

    /**
     * 公司 ID（冗余字段，免去 position → company 两级关联）
     */
    private Long companyId;

    /**
     * 岗位 ID
     */
    private Long positionId;

    /**
     * 投递状态 —— 核心状态机
     */
    private ApplicationStatus status;

    /**
     * 投递渠道
     */
    private ApplicationSource source;

    /**
     * 投递日期
     */
    private LocalDateTime appliedAt;

    /**
     * 备注
     */
    private String notes;

    /**
     * 可选阶段配置，逗号分隔
     * 如 "ASSESSMENT,EXAM,INTERVIEW_3,INTERVIEW_4"
     * null 表示旧数据，需要反推
     */
    private String pipelineConfig;

    /**
     * 当前流水线阶段 key
     * 如：APPLIED / ASSESSMENT / EXAM / INTERVIEW_1 / INTERVIEW_2 / HR_INTERVIEW / OFFER
     * null 表示旧数据，由状态反推
     */
    private String currentStage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
