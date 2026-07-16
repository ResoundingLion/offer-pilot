package com.offerpilot.application.service;

import com.offerpilot.application.entity.Interview;

import java.util.List;

public interface InterviewService {
    Interview findById(Long id);

    List<Interview> findAll();

    Interview create(Interview interview);

    Interview update(Interview interview);

    void deleteById(Long id);

    List<Interview> findByApplicationId(Long applicationId);
}
