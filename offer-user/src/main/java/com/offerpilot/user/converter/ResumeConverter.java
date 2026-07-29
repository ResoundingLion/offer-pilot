package com.offerpilot.user.converter;

import com.offerpilot.user.dto.ResumeCreateRequest;
import com.offerpilot.user.entity.Resume;
import com.offerpilot.user.vo.ResumeVO;

public class ResumeConverter {

    public static ResumeVO convertToVO(Resume resume) {
        ResumeVO vo = new ResumeVO();
        vo.setId(resume.getId());
        vo.setUserId(resume.getUserId());
        vo.setTitle(resume.getTitle());
        vo.setVersion(resume.getVersion());
        vo.setContent(resume.getContent());
        vo.setFileUrl(resume.getFileUrl());
        vo.setSummary(resume.getSummary());
        vo.setIsCurrent(resume.getIsCurrent());
        vo.setCreatedAt(resume.getCreatedAt());
        vo.setUpdatedAt(resume.getUpdatedAt());
        return vo;
    }

    public static Resume convertToEntity(ResumeCreateRequest request) {
        Resume resume = new Resume();
        resume.setTitle(request.getTitle());
        resume.setContent(request.getContent());
        resume.setFileUrl(request.getFileUrl());
        resume.setSummary(request.getSummary());
        // version 由 Service 层自动计算
        // isCurrent 由 Service 层处理
        return resume;
    }
}
