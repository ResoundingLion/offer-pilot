package com.offerpilot.application.service;

import com.offerpilot.application.entity.Application;
import com.offerpilot.application.vo.ApplicationVO;

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
}
