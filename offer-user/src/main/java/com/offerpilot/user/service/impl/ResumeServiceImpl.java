package com.offerpilot.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.offerpilot.common.service.MinioService;
import com.offerpilot.user.entity.Resume;
import com.offerpilot.user.mapper.ResumeMapper;
import com.offerpilot.user.service.PdfService;
import com.offerpilot.user.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeMapper resumeMapper;
    private final MinioService minioService;
    private final PdfService pdfService;

    @Override
    public Resume findById(Long id) {
        return resumeMapper.selectById(id);
    }

    @Override
    public List<Resume> findAllByUserId(Long userId) {
        return resumeMapper.selectList(
                new LambdaQueryWrapper<Resume>()
                        .eq(Resume::getUserId, userId)
                        .orderByDesc(Resume::getUpdatedAt)
        );
    }

    @Override
    @Transactional
    public Resume create(Resume resume) {
        // 计算版本号：同 title 下最大 version + 1
        Resume latest = resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>()
                        .eq(Resume::getUserId, resume.getUserId())
                        .eq(Resume::getTitle, resume.getTitle())
                        .orderByDesc(Resume::getVersion)
                        .last("LIMIT 1")
        );

        if (latest == null) {
            resume.setVersion(1);
            // 第一版自动设为当前版本
            resume.setIsCurrent(true);
        } else {
            resume.setVersion(latest.getVersion() + 1);
            resume.setIsCurrent(false);
        }

        resumeMapper.insert(resume);

        // 如果上传了 PDF，自动提取文本
        autoExtractPdfText(resume);

        return resume;
    }

    /**
     * 如果简历关联了 PDF 文件，自动提取文本并写入 contentText
     */
    private void autoExtractPdfText(Resume resume) {
        String fileUrl = resume.getFileUrl();
        if (fileUrl == null || !fileUrl.toLowerCase().endsWith(".pdf")) {
            return;
        }

        try {
            String text = pdfService.extractText(minioService.download(fileUrl));
            if (text != null) {
                resumeMapper.update(null, new LambdaUpdateWrapper<Resume>()
                        .eq(Resume::getId, resume.getId())
                        .set(Resume::getContentText, text)
                );
                log.info("简历创建时自动提取 PDF 文本: resumeId={}, 文本长度={}",
                        resume.getId(), text.length());
            }
        } catch (Exception e) {
            log.warn("简历创建时 PDF 文本提取失败（不阻断保存）: resumeId={}, error={}",
                    resume.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public Resume update(Resume resume) {
        resumeMapper.updateById(resume);
        return resume;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        resumeMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Resume createNewVersion(Long id) {
        Resume source = resumeMapper.selectById(id);
        if (source == null) {
            return null;
        }

        // 查同 title 下的最大版本号
        Resume latest = resumeMapper.selectOne(
                new LambdaQueryWrapper<Resume>()
                        .eq(Resume::getUserId, source.getUserId())
                        .eq(Resume::getTitle, source.getTitle())
                        .orderByDesc(Resume::getVersion)
                        .last("LIMIT 1")
        );

        Resume newVersion = new Resume();
        newVersion.setUserId(source.getUserId());
        newVersion.setTitle(source.getTitle());
        newVersion.setVersion(latest != null ? latest.getVersion() + 1 : source.getVersion() + 1);
        newVersion.setContent(source.getContent());
        newVersion.setFileUrl(source.getFileUrl());
        newVersion.setSummary(source.getSummary());
        newVersion.setIsCurrent(false);

        resumeMapper.insert(newVersion);
        return newVersion;
    }

    @Override
    @Transactional
    public void updateContentText(Long id, String contentText) {
        resumeMapper.update(null, new LambdaUpdateWrapper<Resume>()
                .eq(Resume::getId, id)
                .set(Resume::getContentText, contentText)
        );
    }

    @Override
    @Transactional
    public Resume setCurrent(Long id) {
        Resume target = resumeMapper.selectById(id);
        if (target == null) {
            return null;
        }

        // 该用户同 title 下所有简历 isCurrent 置 0
        resumeMapper.update(null, new LambdaUpdateWrapper<Resume>()
                .eq(Resume::getUserId, target.getUserId())
                .eq(Resume::getTitle, target.getTitle())
                .eq(Resume::getIsCurrent, true)
                .set(Resume::getIsCurrent, false)
        );

        // 目标置 1
        target.setIsCurrent(true);
        resumeMapper.updateById(target);
        return target;
    }
}
