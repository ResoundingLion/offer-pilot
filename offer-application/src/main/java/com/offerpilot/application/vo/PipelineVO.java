package com.offerpilot.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pipeline 流水线 VO —— 一个投递的阶段灯状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineVO {

    private Long applicationId;
    private String companyName;
    private String positionTitle;
    private LocalDateTime updatedAt;

    /** 该投递的阶段灯列表（从左到右按顺序） */
    private List<StageInfo> stages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StageInfo {
        /** 阶段标识：APPLIED / ASSESSMENT / EXAM / INTERVIEW_1 … OFFER */
        private String stage;
        /** 中文标签：已投递 / 测评 / 笔试 / 一面 … Offer */
        private String label;
        /**
         * 灯状态：
         * COMPLETED — 绿色 ✔
         * ACTIVE    — 青色 ◉ 当前进行中
         * PENDING   — 暗色 ○ 未到
         * FAILED    — 红色 ✕ 止步于此
         * WITHDRAWN — 灰色 ✕ 主动放弃
         */
        private String status;
    }
}
