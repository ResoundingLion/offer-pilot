package com.offerpilot.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.offerpilot.application.entity.Offer;
import com.offerpilot.application.mapper.OfferMapper;
import com.offerpilot.application.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferMapper offerMapper;

    @Override
    public Offer findById(Long id) {
        return offerMapper.selectById(id);
    }

    @Override
    public List<Offer> findAll() {
        return offerMapper.selectList(null);
    }

    @Override
    public Offer create(Offer offer) {
        offerMapper.insert(offer);
        return offer;
    }

    @Override
    public Offer update(Offer offer) {
        offerMapper.updateById(offer);
        return offer;
    }

    @Override
    public void deleteById(Long id) {
        offerMapper.deleteById(id);
    }

    @Override
    public Offer findByApplicationId(Long applicationId) {
        LambdaQueryWrapper<Offer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Offer::getApplicationId, applicationId);
        return offerMapper.selectOne(wrapper);
    }
}
