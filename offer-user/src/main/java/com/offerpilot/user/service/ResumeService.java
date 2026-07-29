package com.offerpilot.user.service;

import com.offerpilot.user.entity.Resume;

import java.util.List;

public interface ResumeService {

    Resume findById(Long id);

    List<Resume> findAllByUserId(Long userId);

    Resume create(Resume resume);

    Resume update(Resume resume);

    void deleteById(Long id);

    Resume createNewVersion(Long id);

    Resume setCurrent(Long id);

    /**
     * 更新简历的 contentText（PDF 提取的纯文本内容）
     */
    void updateContentText(Long id, String contentText);
}
