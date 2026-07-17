package com.offerpilot.application.dto;

import com.offerpilot.application.enums.InterviewRound;
import com.offerpilot.application.enums.InterviewType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewUpdateRequest {

    @NotNull
    private Long id;

    @NotNull
    private Long applicationId;

    @NotNull
    private InterviewRound round;

    @NotNull
    private LocalDateTime scheduledAt;

    private InterviewType interviewType;

    private String location;

    private String interviewer;

    private String feedback;
}
