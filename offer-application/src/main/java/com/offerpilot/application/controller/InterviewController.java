package com.offerpilot.application.controller;


import com.offerpilot.application.converter.InterviewConverter;
import com.offerpilot.application.dto.InterviewCreateRequest;
import com.offerpilot.application.dto.InterviewUpdateRequest;
import com.offerpilot.application.entity.Interview;
import com.offerpilot.application.service.InterviewService;
import com.offerpilot.application.vo.InterviewVO;
import com.offerpilot.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * 获取指定投递的所有面试记录
     * @param appId 投递ID
     * @return 面试列表
     */
    @GetMapping("/api/applications/{appId}/interviews")
    public Result<List<InterviewVO>> getAllInterviews(@PathVariable Long appId) {
        List<Interview> interviews = interviewService.findByApplicationId(appId);
        List<InterviewVO> vos = interviews.stream()
                .map(InterviewConverter::convertToVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 新增面试
     * @param appId 投递ID
     * @param interviewCreateRequest 面试创建请求体
     * @return 面试详情
     */
    @PostMapping("/api/applications/{appId}/interviews")
    public Result<InterviewVO> createInterview(@PathVariable Long appId, @Valid @RequestBody InterviewCreateRequest request) {
        Interview interview = InterviewConverter.convertToEntity(appId, request);
        interviewService.create(interview);
        return Result.created(InterviewConverter.convertToVO(interview));
    }

    /**
     * 更新面试
     * @param id 面试id
     * @param interviewUpdateRequest 面试更新数据
     * @return 面试
     */
    @PutMapping("/api/interviews/{id}")
    public Result<InterviewVO> updateInterview(@PathVariable Long id, @Valid @RequestBody InterviewUpdateRequest request) {
        // 检查存在
        Interview existing = interviewService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }

        Interview interview = InterviewConverter.convertToEntity(request);
        Interview updated = interviewService.update(interview);
        return Result.success(InterviewConverter.convertToVO(updated));
    }

    /**
     * 删除面试
     * @param id 面试id
     * @return 无
     */
    @DeleteMapping("/api/interviews/{id}")
    public Result<Void> deleteInterview(@PathVariable Long id) {
        Interview existing = interviewService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }
        interviewService.deleteById(id);
        return Result.success();
    }

}
