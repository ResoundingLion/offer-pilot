package com.offerpilot.application.service;

import com.offerpilot.application.dto.AdvanceRequest;
import com.offerpilot.application.entity.Application;
import com.offerpilot.application.vo.ApplicationVO;
import com.offerpilot.application.vo.DashboardVO;
import com.offerpilot.application.vo.PipelineVO;

import java.util.List;

public interface ApplicationService {
    Application findById(Long id);

    List<Application> findAll();

    Application create(Application application);

    Application update(Application application);

    void deleteById(Long id);

    List<Application> findAllByUserId(Long userId);

    // 状态变更
    Application updateStatus(Long id, String status);

    // ===== 跨服务组装 =====

    /**
     * 将 Application 实体转换为完整 VO（含 companyName / positionTitle）
     */
    ApplicationVO enrichVO(Application application);

    // ===== 仪表盘统计 =====

    /**
     * 获取仪表盘统计数据
     */
    DashboardVO getDashboardStats();

    // ===== Pipeline 流水线 =====

    /**
     * 获取所有投递的 Pipeline 阶段灯数据（按更新时间倒序）
     */
    List<PipelineVO> getPipeline();

    // ===== 一键推进 =====

    /**
     * 一键推进投递到目标阶段
     * 同时处理：状态变更 + 面试记录创建 + Offer 创建
     */
    ApplicationVO advance(Long id, AdvanceRequest request);
}
