package com.offerpilot.user.controller.internal;

import com.offerpilot.api.dto.PositionDTO;
import com.offerpilot.user.entity.Position;
import com.offerpilot.user.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部接口 —— 供其他服务通过 Feign 调用
 */
@RestController
@RequestMapping("/internal/positions")
@RequiredArgsConstructor
public class PositionInternalController {

    private final PositionService positionService;

    @GetMapping("/{id}")
    public PositionDTO getPositionById(@PathVariable Long id) {
        Position position = positionService.findById(id);
        if (position == null) {
            return new PositionDTO();
        }
        return new PositionDTO(position.getId(), position.getTitle());
    }
}
