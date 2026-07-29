package com.offerpilot.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResumeCreateRequest {

    @NotBlank
    private String title;

    private String content;

    private String fileUrl;

    private String summary;
}
