package com.offerpilot.user.dto;

import lombok.Data;

@Data
public class ResumeUpdateRequest {

    private String content;

    private String fileUrl;

    private String summary;
}
