package com.offerpilot.application.vo;

import com.offerpilot.application.enums.InterviewResult;
import com.offerpilot.application.enums.InterviewRound;
import com.offerpilot.application.enums.InterviewType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewVO {
    private Long id;
    private Long applicationId;
    private InterviewRound round;
    private LocalDateTime scheduledAt;
    private InterviewType interviewType;
    private String location;
    private String interviewer;
    private InterviewResult result;
    private String feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
