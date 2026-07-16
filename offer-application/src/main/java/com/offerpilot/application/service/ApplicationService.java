package com.offerpilot.application.service;

import com.offerpilot.application.entity.Application;

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
}
