package com.offerpilot.application.controller.internal;

import com.offerpilot.api.dto.InterviewDTO;
import com.offerpilot.application.entity.Interview;
import com.offerpilot.application.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 内部接口 —— 供 AI 服务通过 Feign 获取面试记录
 */
@RestController
@RequestMapping("/internal/interviews")
@RequiredArgsConstructor
public class InterviewInternalController {

    private final InterviewService interviewService;

    /**
     * 获取某投递的所有面试记录
     */
    @GetMapping
    public List<InterviewDTO> getInterviews(@RequestParam Long applicationId) {
        List<Interview> interviews = interviewService.findByApplicationId(applicationId);
        return interviews.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private InterviewDTO toDTO(Interview iv) {
        return InterviewDTO.builder()
                .id(iv.getId())
                .applicationId(iv.getApplicationId())
                .round(iv.getRound() != null ? iv.getRound().name() : null)
                .scheduledAt(iv.getScheduledAt())
                .interviewType(iv.getInterviewType() != null ? iv.getInterviewType().name() : null)
                .location(iv.getLocation())
                .interviewer(iv.getInterviewer())
                .result(iv.getResult() != null ? iv.getResult().name() : null)
                .feedback(iv.getFeedback())
                .build();
    }
}
