package com.offerpilot.application.dto;

import com.offerpilot.application.enums.InterviewResult;
import com.offerpilot.application.enums.InterviewRound;
import com.offerpilot.application.enums.InterviewType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 一键推进请求 —— 推进投递到下一阶段
 * <p>
 * 一个请求同时完成：状态变更 + 可选的面试/Offer 记录创建
 * </p>
 */
@Data
public class AdvanceRequest {

    /**
     * 目标阶段 key（来自 Pipeline 的阶段标识）
     * 如：ASSESSMENT / EXAM / INTERVIEW_1 / INTERVIEW_2 / HR_INTERVIEW / OFFER / REJECTED / WITHDRAWN
     */
    private String targetStage;

    // ========== 面试信息（推进到面试轮次时填写） ==========

    private InterviewRound interviewRound;
    private LocalDateTime interviewScheduledAt;
    private InterviewType interviewType;
    private String interviewLocation;
    private String interviewInterviewer;
    private InterviewResult interviewResult;
    private String interviewFeedback;

    // ========== Offer 信息（推进到 Offer 时填写） ==========

    private String offerSalary;
    private String offerBonus;
    private String offerStock;
    private String offerBenefits;
    private LocalDate offerDeadline;
    private String offerRemark;
}
