package com.offerpilot.application.converter;

import com.offerpilot.application.dto.InterviewCreateRequest;
import com.offerpilot.application.dto.InterviewUpdateRequest;
import com.offerpilot.application.entity.Interview;
import com.offerpilot.application.vo.InterviewVO;

public class InterviewConverter {

    public static InterviewVO convertToVO(Interview interview) {
        InterviewVO vo = new InterviewVO();
        vo.setId(interview.getId());
        vo.setApplicationId(interview.getApplicationId());
        vo.setRound(interview.getRound());
        vo.setScheduledAt(interview.getScheduledAt());
        vo.setInterviewType(interview.getInterviewType());
        vo.setLocation(interview.getLocation());
        vo.setInterviewer(interview.getInterviewer());
        vo.setResult(interview.getResult());
        vo.setFeedback(interview.getFeedback());
        vo.setCreatedAt(interview.getCreatedAt());
        vo.setUpdatedAt(interview.getUpdatedAt());
        return vo;
    }

    /**
     * @param applicationId 从路径 @PathVariable 传入
     * @param request       请求体 DTO
     */
    public static Interview convertToEntity(Long applicationId, InterviewCreateRequest request) {
        Interview interview = new Interview();
        interview.setApplicationId(applicationId);
        interview.setInterviewType(request.getInterviewType());
        interview.setInterviewer(request.getInterviewer());
        interview.setScheduledAt(request.getScheduledAt());
        interview.setRound(request.getRound());
        interview.setLocation(request.getLocation());
        return interview;
    }

    public static Interview convertToEntity(InterviewUpdateRequest request) {
        Interview interview = new Interview();
        interview.setId(request.getId());
        interview.setApplicationId(request.getApplicationId());
        interview.setRound(request.getRound());
        interview.setScheduledAt(request.getScheduledAt());
        interview.setInterviewType(request.getInterviewType());
        interview.setInterviewer(request.getInterviewer());
        interview.setLocation(request.getLocation());
        interview.setFeedback(request.getFeedback());
        interview.setResult(request.getResult());
        return interview;
    }
}
