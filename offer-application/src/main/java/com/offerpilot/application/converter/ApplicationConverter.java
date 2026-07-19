package com.offerpilot.application.converter;

import com.offerpilot.application.dto.ApplicationCreateRequest;
import com.offerpilot.application.dto.ApplicationUpdateRequest;
import com.offerpilot.application.entity.Application;
import com.offerpilot.application.enums.ApplicationStatus;
import com.offerpilot.application.vo.ApplicationVO;

public class ApplicationConverter {

    public static ApplicationVO convertToVO(Application application) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(application.getId());
        vo.setUserId(application.getUserId());
        vo.setCompanyId(application.getCompanyId());
        vo.setPositionId(application.getPositionId());
        vo.setStatus(application.getStatus());
        vo.setSource(application.getSource());
        vo.setAppliedAt(application.getAppliedAt());
        vo.setNotes(application.getNotes());
        vo.setCreatedAt(application.getCreatedAt());
        vo.setUpdatedAt(application.getUpdatedAt());
        return vo;
    }

    public static Application convertToEntity(ApplicationCreateRequest request) {
        Application application = new Application();
        application.setUserId(request.getUserId());
        application.setCompanyId(request.getCompanyId());
        application.setPositionId(request.getPositionId());
        application.setSource(request.getSource());
        application.setAppliedAt(request.getAppliedAt());
        application.setNotes(request.getNotes());
        // 新增投递默认状态为 SAVED
        application.setStatus(ApplicationStatus.SAVED);
        return application;
    }

    public static Application convertToEntity(ApplicationUpdateRequest request) {
        Application application = new Application();
        application.setId(request.getId());
        application.setCompanyId(request.getCompanyId());
        application.setPositionId(request.getPositionId());
        application.setSource(request.getSource());
        application.setAppliedAt(request.getAppliedAt());
        application.setNotes(request.getNotes());
        // status 不更新——状态变更走专门的 PATCH 接口
        return application;
    }
}
