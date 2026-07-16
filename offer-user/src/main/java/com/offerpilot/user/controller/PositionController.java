package com.offerpilot.user.controller;

import com.offerpilot.common.result.Result;
import com.offerpilot.user.dto.PositionCreateRequest;
import com.offerpilot.user.dto.PositionUpdateRequest;
import com.offerpilot.user.entity.Position;
import com.offerpilot.user.service.PositionService;
import com.offerpilot.user.vo.PositionVO;
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
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping("/{id}")
    public Result<PositionVO> getPositionById(@PathVariable Long id) {
        Position position = positionService.findById(id);
        if (position == null) {
            return Result.notFound();
        }
        return Result.success(convertToVO(position));
    }

    @GetMapping
    public Result<List<PositionVO>> getAllPositions() {
        List<Position> positions = positionService.findAll();
        List<PositionVO> vos = positions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    @PostMapping
    public Result<PositionVO> createPosition(@Valid @RequestBody PositionCreateRequest request) {
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

        Position created = positionService.create(position);
        return Result.created(convertToVO(created));
    }

    @PutMapping
    public Result<PositionVO> updatePosition(@Valid @RequestBody PositionUpdateRequest request) {
        Position existing = positionService.findById(request.getId());
        if (existing == null) {
            return Result.notFound();
        }

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

        Position updated = positionService.update(position);
        return Result.success(convertToVO(updated));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePosition(@PathVariable Long id) {
        Position existing = positionService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }
        positionService.deleteById(id);
        return Result.success();
    }

    private PositionVO convertToVO(Position position) {
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
}
