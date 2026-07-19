package com.offerpilot.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    /** 总投递数 */
    private long totalApplications;

    /** 面试中的数量 */
    private long interviewCount;

    /** 已拿 Offer 的数量 */
    private long offerCount;

    /** 进行中的数量（非终止态） */
    private long activeCount;

    /** 近 14 天每日新增投递趋势 */
    private List<DailyTrendItem> dailyTrend;

    /** 各渠道投递分布 */
    private List<SourceItem> sourceDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrendItem {
        private String date;
        private long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceItem {
        private String source;
        private long count;
    }
}
