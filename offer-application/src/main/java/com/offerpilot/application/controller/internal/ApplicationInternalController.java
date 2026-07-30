package com.offerpilot.application.controller.internal;

import com.offerpilot.api.dto.ApplicationDTO;
import com.offerpilot.application.entity.Application;
import com.offerpilot.application.service.ApplicationService;
import com.offerpilot.application.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 内部接口 —— 供 AI 服务通过 Feign 获取投递数据
 */
@RestController
@RequestMapping("/internal/applications")
@RequiredArgsConstructor
public class ApplicationInternalController {

    private final ApplicationService applicationService;

    /**
     * 获取用户全部投递记录
     */
    @GetMapping
    public List<ApplicationDTO> getApplications(@RequestParam Long userId) {
        List<Application> applications = applicationService.findAllByUserId(userId);
        return applications.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取 Dashboard 统计数据
     */
    @GetMapping("/dashboard/stats")
    public DashboardVO getDashboardStats(@RequestParam Long userId) {
        return applicationService.getDashboardStats(userId);
    }

    private ApplicationDTO toDTO(Application a) {
        return ApplicationDTO.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .companyId(a.getCompanyId())
                .positionId(a.getPositionId())
                .status(a.getStatus() != null ? a.getStatus().name() : null)
                .source(a.getSource() != null ? a.getSource().name() : null)
                .appliedAt(a.getAppliedAt())
                .notes(a.getNotes())
                .pipelineConfig(a.getPipelineConfig())
                .currentStage(a.getCurrentStage())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
