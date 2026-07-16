package com.offerpilot.user.service;

import com.offerpilot.user.entity.Position;

import java.util.List;

public interface PositionService {
    Position findById(Long id);

    List<Position> findAll();

    Position create(Position position);

    Position update(Position position);

    void deleteById(Long id);
}
