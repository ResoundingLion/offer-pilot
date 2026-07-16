package com.offerpilot.user.service.impl;

import com.offerpilot.user.entity.Position;
import com.offerpilot.user.mapper.PositionMapper;
import com.offerpilot.user.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionMapper positionMapper;

    @Override
    public Position findById(Long id) {
        return positionMapper.selectById(id);
    }

    @Override
    public List<Position> findAll() {
        return positionMapper.selectList(null);
    }

    @Override
    public Position create(Position position) {
        positionMapper.insert(position);
        return position;
    }

    @Override
    public Position update(Position position) {
        positionMapper.updateById(position);
        return position;
    }

    @Override
    public void deleteById(Long id) {
        positionMapper.deleteById(id);
    }
}
