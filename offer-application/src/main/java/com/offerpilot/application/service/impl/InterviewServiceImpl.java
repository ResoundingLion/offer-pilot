package com.offerpilot.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.offerpilot.application.entity.Interview;
import com.offerpilot.application.mapper.InterviewMapper;
import com.offerpilot.application.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewMapper interviewMapper;
    @Override
    public Interview findById(Long id) {
        return interviewMapper.selectById(id);
    }

    @Override
    public List<Interview> findAll() {
        return interviewMapper.selectList(null);
    }

    @Override
    public Interview create(Interview interview) {
        interviewMapper.insert(interview);
        return interview;
    }

    @Override
    public Interview update(Interview interview) {
        interviewMapper.updateById(interview);
        return interview;
    }

    @Override
    public void deleteById(Long id) {
        interviewMapper.deleteById(id);
    }

    @Override
    public List<Interview> findByApplicationId(Long applicationId) {
        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interview::getApplicationId,applicationId)
                .orderByAsc(Interview::getScheduledAt);
        return interviewMapper.selectList(wrapper);
    }
}
