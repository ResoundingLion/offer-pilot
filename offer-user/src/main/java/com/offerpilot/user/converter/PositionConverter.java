package com.offerpilot.user.converter;

import com.offerpilot.user.dto.PositionCreateRequest;
import com.offerpilot.user.dto.PositionUpdateRequest;
import com.offerpilot.user.entity.Position;
import com.offerpilot.user.vo.PositionVO;

public class PositionConverter {

    public static PositionVO convertToVO(Position position) {
        PositionVO vo = new PositionVO();
        vo.setId(position.getId());
        vo.setCompanyId(position.getCompanyId());
        vo.setTitle(position.getTitle());
        vo.setSalaryMin(position.getSalaryMin());
        vo.setSalaryMax(position.getSalaryMax());
        vo.setCity(position.getCity());
        vo.setEducation(position.getEducation());
        vo.setExperience(position.getExperience());
        vo.setEmploymentType(position.getEmploymentType());
        vo.setDescription(position.getDescription());
        vo.setStatus(position.getStatus());
        vo.setDeadline(position.getDeadline());
        vo.setCreatedAt(position.getCreatedAt());
        vo.setUpdatedAt(position.getUpdatedAt());
        return vo;
    }

    public static Position convertToEntity(PositionCreateRequest request) {
        Position position = new Position();
        position.setCompanyId(request.getCompanyId());
        position.setTitle(request.getTitle());
        position.setSalaryMin(request.getSalaryMin());
        position.setSalaryMax(request.getSalaryMax());
        position.setCity(request.getCity());
        position.setEducation(request.getEducation());
        position.setExperience(request.getExperience());
        position.setEmploymentType(request.getEmploymentType());
        position.setDescription(request.getDescription());
        position.setDeadline(request.getDeadline());
        return position;
    }

    public static Position convertToEntity(PositionUpdateRequest request) {
        Position position = new Position();
        position.setId(request.getId());
        position.setCompanyId(request.getCompanyId());
        position.setTitle(request.getTitle());
        position.setSalaryMin(request.getSalaryMin());
        position.setSalaryMax(request.getSalaryMax());
        position.setCity(request.getCity());
        position.setEducation(request.getEducation());
        position.setExperience(request.getExperience());
        position.setEmploymentType(request.getEmploymentType());
        position.setDescription(request.getDescription());
        position.setDeadline(request.getDeadline());
        return position;
    }
}
