package com.offerpilot.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.offerpilot.application.entity.Application;
import com.offerpilot.application.enums.ApplicationStatus;
import com.offerpilot.application.mapper.ApplicationMapper;
import com.offerpilot.application.service.ApplicationService;
import com.offerpilot.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;

import static com.offerpilot.common.result.ResultCode.BAD_REQUEST;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationMapper applicationMapper;
    @Override
    public Application findById(Long id) {
        return applicationMapper.selectById(id);
    }

    @Override
    public List<Application> findAll() {
        return applicationMapper.selectList(null);
    }

    @Override
    public Application create(Application application) {
        applicationMapper.insert(application);
        return application;
    }

    @Override
    public Application update(Application application) {
        applicationMapper.updateById(application);
        return application;
    }

    @Override
    public void deleteById(Long id) {
        applicationMapper.deleteById(id);
    }

    @Override
    public List<Application> findAllByUserId(Long userId) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Application::getUserId,userId)
                .orderByDesc(Application::getUpdatedAt);
        return applicationMapper.selectList(wrapper);
    }

    @Override
    public Application updateStatus(Long id, String status) {
        // 1、查投递记录
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            return null;
        }

        // 2、字符串 → 枚举
        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(BAD_REQUEST, "无效的投递状态: " + status);
        }

        ApplicationStatus currentStatus = application.getStatus();

        // 3、终止态不能再变更
        if (currentStatus == ApplicationStatus.REJECTED || currentStatus == ApplicationStatus.WITHDRAWN) {
            throw new BusinessException(BAD_REQUEST, "已终止的投递无法变更状态");
        }

        // 4、校验流转是否合法
        Set<ApplicationStatus> allowed = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(newStatus)) {
            throw new BusinessException(BAD_REQUEST,
                    String.format("不允许从 %s 变更为 %s", currentStatus, newStatus));
        }

        // 5、通过，更新
        application.setStatus(newStatus);
        applicationMapper.updateById(application);
        return application;
    }


    // 当前状态 → 允许跳转到的状态集合
    private static final Map<ApplicationStatus, Set<ApplicationStatus>>
            ALLOWED_TRANSITIONS = Map.of(
            ApplicationStatus.SAVED, Set.of(ApplicationStatus.APPLIED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.APPLIED, Set.of(ApplicationStatus.ONLINE_ASSESSMENT, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.ONLINE_ASSESSMENT, Set.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.INTERVIEW, Set.of(ApplicationStatus.HR_INTERVIEW, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.HR_INTERVIEW, Set.of(ApplicationStatus.OFFER, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.OFFER, Set.of(ApplicationStatus.WITHDRAWN)
            // REJECTED 和 WITHDRAWN 是终止态，没有可跳转的目标，所以不在这里
    );

}
