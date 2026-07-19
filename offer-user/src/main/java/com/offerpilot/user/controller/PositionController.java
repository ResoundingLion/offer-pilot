package com.offerpilot.user.controller;

import com.offerpilot.common.result.Result;
import com.offerpilot.user.converter.PositionConverter;
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
        return Result.success(PositionConverter.convertToVO(position));
    }

    @GetMapping
    public Result<List<PositionVO>> getAllPositions() {
        List<Position> positions = positionService.findAll();
        List<PositionVO> vos = positions.stream()
                .map(PositionConverter::convertToVO)
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    @PostMapping
    public Result<PositionVO> createPosition(@Valid @RequestBody PositionCreateRequest request) {
        Position position = PositionConverter.convertToEntity(request);
        Position created = positionService.create(position);
        return Result.created(PositionConverter.convertToVO(created));
    }

    @PutMapping
    public Result<PositionVO> updatePosition(@Valid @RequestBody PositionUpdateRequest request) {
        Position existing = positionService.findById(request.getId());
        if (existing == null) {
            return Result.notFound();
        }

        Position position = PositionConverter.convertToEntity(request);
        Position updated = positionService.update(position);
        return Result.success(PositionConverter.convertToVO(updated));
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

}
