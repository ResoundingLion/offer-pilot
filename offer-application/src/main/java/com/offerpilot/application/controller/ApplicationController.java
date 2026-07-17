package com.offerpilot.application.controller;

import com.offerpilot.application.dto.ApplicationCreateRequest;
import com.offerpilot.application.dto.ApplicationUpdateRequest;
import com.offerpilot.application.dto.StatusUpdateRequest;
import com.offerpilot.application.entity.Application;
import com.offerpilot.application.enums.ApplicationStatus;
import com.offerpilot.application.service.ApplicationService;
import com.offerpilot.application.vo.ApplicationVO;
import com.offerpilot.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // ====== 查询类接口 ======

    /**
     * GET /api/applications —— 全部投递列表
     */
    @GetMapping
    public Result<List<ApplicationVO>> getAllApplications() {
        List<Application> applications = applicationService.findAll();
        List<ApplicationVO> vos = applications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * GET /api/applications/{id} —— 投递详情
     */
    @GetMapping("/{id}")
    public Result<ApplicationVO> getApplicationById(@PathVariable Long id) {
        Application application = applicationService.findById(id);
        if (application == null) {
            return Result.notFound();
        }
        return Result.success(convertToVO(application));
    }

    // ====== 写操作 ======

    /**
     * POST /api/applications —— 新增投递
     * 接收 ApplicationCreateRequest（DTO），手动转成 Entity 再入库。
     * 为什么不直接传 Entity？
     *   1. Entity 有 id/createdAt/updatedAt 等前端不该传的字段
     *   2. DTO 可以用 @NotNull/@NotBlank 做参数校验
     *   3. Entity 和 DTO 职责分离，互不影响
     */
    @PostMapping
    public Result<ApplicationVO> createApplication(@Valid @RequestBody ApplicationCreateRequest request) {
        Application application = new Application();
        application.setUserId(request.getUserId());
        application.setCompanyId(request.getCompanyId());
        application.setPositionId(request.getPositionId());
        application.setSource(request.getSource());
        application.setAppliedAt(request.getAppliedAt());
        application.setNotes(request.getNotes());
        // status 默认投递为 SAVED（收藏/待投递），后续通过 PATCH 状态流转变更
        application.setStatus(ApplicationStatus.SAVED);

        Application created = applicationService.create(application);
        return Result.created(convertToVO(created));
    }

    /**
     * PUT /api/applications/{id} —— 更新投递
     *
     * 为什么 PUT 的 id 从路径取，又从请求体取？
     *   - 路径 {id} 用于路由匹配（让 REST 语义清晰）
     *   - 请求体里的 id 用于确保更新的是正确记录（双重校验）
     *   实际代码中我们用请求体里的 id 做更新。
     */
    @PutMapping("/{id}")
    public Result<ApplicationVO> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationUpdateRequest request) {
        // 先检查存在性
        Application existing = applicationService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }

        Application application = new Application();
        application.setId(request.getId());
        application.setCompanyId(request.getCompanyId());
        application.setPositionId(request.getPositionId());
        application.setSource(request.getSource());
        application.setAppliedAt(request.getAppliedAt());
        application.setNotes(request.getNotes());
        // status 不更新——状态变更走专门的 PATCH 接口

        Application updated = applicationService.update(application);
        return Result.success(convertToVO(updated));
    }

    // ====== 状态流转 ======

    /**
     * PATCH /api/applications/{id}/status —— 状态流转
     *
     * 为什么用 PATCH 而不是 PUT？
     *   PUT 是全量替换，语义上是「把整个资源替换成请求体里的样子」
     *   PATCH 是部分更新，语义上是「只改这几个字段」
     *   这里只改 status 一个字段，PATCH 更准确
     *
     * 为什么状态变更要做成独立接口而不是放 PUT 里？
     *   状态流转有业务规则（SAVED 不能直接跳到 OFFER），
     *   如果放 PUT 里，每次更新都要检查状态变化逻辑，
     *   分离出来让 PUT 只改基本信息，PATCH 专门处理状态机。
     */
    @PatchMapping("/{id}/status")
    public Result<ApplicationVO> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        Application updated = applicationService.updateStatus(id, request.getStatus());
        if (updated == null) {
            return Result.notFound();
        }
        return Result.success(convertToVO(updated));
    }

    // ====== 删除 ======

    /**
     * DELETE /api/applications/{id} —— 删除投递
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteApplication(@PathVariable Long id) {
        Application existing = applicationService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }
        applicationService.deleteById(id);
        return Result.success();
    }

    // ====== 转换方法 ======

    /**
     * Entity → VO 转换
     *
     * 为什么需要这个方法？
     *   不能直接把 Entity 返回给前端，因为：
     *   1. Entity 可能包含敏感字段（虽然目前没有）
     *   2. Entity 可能关联了懒加载的对象（序列化会报错）
     *   3. VO 可以自由组合字段，不一定要和 Entity 一一对应
     *      （比如后面 ApplicationVO 可以冗余显示 companyName 和 positionTitle）
     */
    private ApplicationVO convertToVO(Application application) {
        ApplicationVO vo = new ApplicationVO();
        vo.setId(application.getId());
        vo.setUserId(application.getUserId());
        vo.setCompanyId(application.getCompanyId());
        vo.setPositionId(application.getPositionId());
        vo.setStatus(application.getStatus());
        vo.setSource(application.getSource());
        vo.setAppliedAt(application.getAppliedAt());
        vo.setNotes(application.getNotes());
        vo.setCreatedAt(application.getCreatedAt());
        vo.setUpdatedAt(application.getUpdatedAt());
        return vo;
    }
}
