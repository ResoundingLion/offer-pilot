package com.offerpilot.user.converter;

import com.offerpilot.user.dto.CompanyCreateRequest;
import com.offerpilot.user.dto.CompanyUpdateRequest;
import com.offerpilot.user.entity.Company;
import com.offerpilot.user.vo.CompanyVO;

public class CompanyConverter {

    public static CompanyVO convertToVO(Company company) {
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

    public static Company convertToEntity(CompanyCreateRequest request) {
        Company company = new Company();
        company.setUserId(request.getUserId());
        company.setName(request.getName());
        company.setIndustry(request.getIndustry());
        company.setWebsite(request.getWebsite());
        company.setLocation(request.getLocation());
        company.setSize(request.getSize());
        company.setDescription(request.getDescription());
        return company;
    }

    public static Company convertToEntity(CompanyUpdateRequest request) {
        Company company = new Company();
        company.setId(request.getId());
        company.setUserId(request.getUserId());
        company.setName(request.getName());
        company.setIndustry(request.getIndustry());
        company.setWebsite(request.getWebsite());
        company.setLocation(request.getLocation());
        company.setSize(request.getSize());
        company.setDescription(request.getDescription());
        return company;
    }
}
