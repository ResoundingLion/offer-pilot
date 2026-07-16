package com.offerpilot.user.service.impl;

import com.offerpilot.user.entity.Company;
import com.offerpilot.user.mapper.CompanyMapper;
import com.offerpilot.user.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyMapper companyMapper;

    @Override
    public Company findById(Long id) {
        return companyMapper.selectById(id);
    }

    @Override
    public List<Company> findAll() {
        return companyMapper.selectList(null);
    }

    @Override
    public Company create(Company company) {
        companyMapper.insert(company);
        return company;
    }

    @Override
    public Company update(Company company) {
        companyMapper.updateById(company);
        return company;
    }

    @Override
    public void deleteById(Long id) {
        companyMapper.deleteById(id);
    }
}
