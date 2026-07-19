package com.offerpilot.user.controller.internal;

import com.offerpilot.api.dto.CompanyDTO;
import com.offerpilot.user.entity.Company;
import com.offerpilot.user.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部接口 —— 供其他服务通过 Feign 调用
 * 不经过网关、不走 Result 包装，直接返回原始 DTO
 */
@RestController
@RequestMapping("/internal/companies")
@RequiredArgsConstructor
public class CompanyInternalController {

    private final CompanyService companyService;

    @GetMapping("/{id}")
    public CompanyDTO getCompanyById(@PathVariable Long id) {
        Company company = companyService.findById(id);
        // 找不到时返回空 DTO，Feign 客户端通过 try-catch 处理
        if (company == null) {
            return new CompanyDTO();
        }
        return new CompanyDTO(company.getId(), company.getName());
    }
}
