package com.offerpilot.user.controller;

import com.offerpilot.common.result.Result;
import com.offerpilot.user.converter.ResumeConverter;
import com.offerpilot.user.dto.ResumeCreateRequest;
import com.offerpilot.user.dto.ResumeUpdateRequest;
import com.offerpilot.user.entity.Resume;
import com.offerpilot.user.service.ResumeService;
import com.offerpilot.user.vo.ResumeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * GET /api/resumes —— 当前用户的全部简历
     */
    @GetMapping
    public Result<List<ResumeVO>> getAllResumes(
            @RequestHeader("X-User-Id") Long userId) {
        List<Resume> resumes = resumeService.findAllByUserId(userId);
        List<ResumeVO> vos = resumes.stream()
                .map(ResumeConverter::convertToVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * GET /api/resumes/{id} —— 简历详情
     */
    @GetMapping("/{id}")
    public Result<ResumeVO> getResumeById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        Resume resume = resumeService.findById(id);
        if (resume == null) {
            return Result.notFound();
        }
        if (!resume.getUserId().equals(userId)) {
            return Result.forbidden();
        }
        return Result.success(ResumeConverter.convertToVO(resume));
    }

    /**
     * POST /api/resumes —— 创建新简历
     * 同一 title 下自动递增版本号
     */
    @PostMapping
    public Result<ResumeVO> createResume(
            @Valid @RequestBody ResumeCreateRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        Resume resume = ResumeConverter.convertToEntity(request);
        resume.setUserId(userId);
        Resume created = resumeService.create(resume);
        return Result.created(ResumeConverter.convertToVO(created));
    }

    /**
     * PUT /api/resumes/{id} —— 更新简历内容
     * 只能更新 content / fileUrl / summary，title 和 version 不可改
     */
    @PutMapping("/{id}")
    public Result<ResumeVO> updateResume(
            @PathVariable Long id,
            @Valid @RequestBody ResumeUpdateRequest request,
            @RequestHeader("X-User-Id") Long userId) {
        Resume existing = resumeService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.forbidden();
        }

        // 只更新前端传入的字段
        if (request.getContent() != null) {
            existing.setContent(request.getContent());
        }
        if (request.getFileUrl() != null) {
            existing.setFileUrl(request.getFileUrl());
        }
        if (request.getSummary() != null) {
            existing.setSummary(request.getSummary());
        }

        Resume updated = resumeService.update(existing);
        return Result.success(ResumeConverter.convertToVO(updated));
    }

    /**
     * DELETE /api/resumes/{id} —— 删除某个简历版本
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteResume(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        Resume existing = resumeService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.forbidden();
        }
        resumeService.deleteById(id);
        return Result.success();
    }

    /**
     * PATCH /api/resumes/{id}/new-version —— 基于当前版本创建新版本
     */
    @PatchMapping("/{id}/new-version")
    public Result<ResumeVO> createNewVersion(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        Resume existing = resumeService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.forbidden();
        }
        Resume newVersion = resumeService.createNewVersion(id);
        if (newVersion == null) {
            return Result.notFound();
        }
        return Result.created(ResumeConverter.convertToVO(newVersion));
    }

    /**
     * PATCH /api/resumes/{id}/current —— 设为当前使用版本
     */
    @PatchMapping("/{id}/current")
    public Result<ResumeVO> setCurrent(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        Resume existing = resumeService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.forbidden();
        }
        Resume current = resumeService.setCurrent(id);
        if (current == null) {
            return Result.notFound();
        }
        return Result.success(ResumeConverter.convertToVO(current));
    }
}
