package com.offerpilot.application.controller;

import com.offerpilot.application.service.ApplicationService;
import com.offerpilot.application.vo.DashboardVO;
import com.offerpilot.application.vo.PipelineVO;
import com.offerpilot.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 仪表盘统计接口
 * 一条 API 聚合所有统计指标，前端只需请求一次
 */
@RestController
@RequestMapping("/api/applications/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ApplicationService applicationService;

    @GetMapping("/stats")
    public Result<DashboardVO> getStats(
            @RequestHeader("X-User-Id") Long userId) {
        DashboardVO stats = applicationService.getDashboardStats(userId);
        return Result.success(stats);
    }

    /**
     * GET /api/applications/dashboard/pipeline —— 当前用户的投递阶段灯流水线
     */
    @GetMapping("/pipeline")
    public Result<List<PipelineVO>> getPipeline(
            @RequestHeader("X-User-Id") Long userId) {
        return Result.success(applicationService.getPipeline(userId));
    }
}
