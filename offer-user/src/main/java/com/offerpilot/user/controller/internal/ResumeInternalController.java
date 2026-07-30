package com.offerpilot.user.controller.internal;

import com.offerpilot.api.dto.ResumeDTO;
import com.offerpilot.user.entity.Resume;
import com.offerpilot.user.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部接口 —— 供 AI 服务通过 Feign 获取简历
 */
@RestController
@RequestMapping("/internal/resumes")
@RequiredArgsConstructor
public class ResumeInternalController {

    private final ResumeService resumeService;

    /**
     * 获取用户当前使用的简历（含 contentText，供 AI 分析简历匹配度）
     */
    @GetMapping("/active")
    public ResumeDTO getActiveResume(@RequestParam Long userId) {
        List<Resume> resumes = resumeService.findAllByUserId(userId);
        return resumes.stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsCurrent()))
                .findFirst()
                .map(this::toDTO)
                .orElse(null);
    }

    private ResumeDTO toDTO(Resume r) {
        ResumeDTO dto = new ResumeDTO();
        dto.setId(r.getId());
        dto.setUserId(r.getUserId());
        dto.setTitle(r.getTitle());
        dto.setVersion(r.getVersion());
        dto.setContent(r.getContent());
        dto.setFileUrl(r.getFileUrl());
        dto.setSummary(r.getSummary());
        dto.setContentText(r.getContentText());
        dto.setIsCurrent(r.getIsCurrent());
        return dto;
    }
}
