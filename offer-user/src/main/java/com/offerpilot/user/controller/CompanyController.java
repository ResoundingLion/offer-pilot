package com.offerpilot.user.controller;

import com.offerpilot.common.result.Result;
import com.offerpilot.user.dto.CompanyCreateRequest;
import com.offerpilot.user.dto.CompanyUpdateRequest;
import com.offerpilot.user.entity.Company;
import com.offerpilot.user.service.CompanyService;
import com.offerpilot.user.vo.CompanyVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/{id}")
    public Result<CompanyVO> getCompanyById(@PathVariable Long id) {
        Company company = companyService.findById(id);
        if (company == null) {
            return Result.notFound();
        }
        return Result.success(convertToVO(company));
    }

    @GetMapping
    public Result<List<CompanyVO>> getAllCompanies() {
        List<Company> companies = companyService.findAll();
        List<CompanyVO> vos = companies.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    @PostMapping
    public Result<CompanyVO> createCompany(@Valid @RequestBody CompanyCreateRequest request) {
        Company company = new Company();
        company.setUserId(request.getUserId());
        company.setName(request.getName());
        company.setIndustry(request.getIndustry());
        company.setWebsite(request.getWebsite());
        company.setLocation(request.getLocation());
        company.setSize(request.getSize());
        company.setDescription(request.getDescription());

        Company created = companyService.create(company);
        return Result.created(convertToVO(created));
    }

    @PutMapping
    public Result<CompanyVO> updateCompany(@Valid @RequestBody CompanyUpdateRequest request) {
        Company existing = companyService.findById(request.getId());
        if (existing == null) {
            return Result.notFound();
        }

        Company company = new Company();
        company.setId(request.getId());
        company.setUserId(request.getUserId());
        company.setName(request.getName());
        company.setIndustry(request.getIndustry());
        company.setWebsite(request.getWebsite());
        company.setLocation(request.getLocation());
        company.setSize(request.getSize());
        company.setDescription(request.getDescription());

        Company updated = companyService.update(company);
        return Result.success(convertToVO(updated));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCompany(@PathVariable Long id) {
        Company existing = companyService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }
        companyService.deleteById(id);
        return Result.success();
    }

    private CompanyVO convertToVO(Company company) {
        CompanyVO vo = new CompanyVO();
        vo.setId(company.getId());
        vo.setUserId(company.getUserId());
        vo.setName(company.getName());
        vo.setIndustry(company.getIndustry());
        vo.setWebsite(company.getWebsite());
        vo.setLocation(company.getLocation());
        vo.setSize(company.getSize());
        vo.setDescription(company.getDescription());
        vo.setCreatedAt(company.getCreatedAt());
        vo.setUpdatedAt(company.getUpdatedAt());
        return vo;
    }
}
