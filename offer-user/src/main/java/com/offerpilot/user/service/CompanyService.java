package com.offerpilot.user.service;

import com.offerpilot.user.entity.Company;

import java.util.List;

public interface CompanyService {
    Company findById(Long id);

    List<Company> findAll();

    Company create(Company company);

    Company update(Company company);

    void deleteById(Long id);
}
