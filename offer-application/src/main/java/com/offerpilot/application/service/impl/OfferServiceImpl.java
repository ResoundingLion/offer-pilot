package com.offerpilot.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.offerpilot.application.entity.Offer;
import com.offerpilot.application.enums.OfferStatus;
import com.offerpilot.application.mapper.OfferMapper;
import com.offerpilot.application.service.OfferService;
import com.offerpilot.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;

import static com.offerpilot.common.result.ResultCode.BAD_REQUEST;
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

    @Override
    public Offer updateStatus(Long id, String status) {
        Offer offer = offerMapper.selectById(id);
        if (offer == null) {
            return null;
        }

        OfferStatus newStatus;
        try {
            newStatus = OfferStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(BAD_REQUEST, "无效的 Offer 状态: " + status);
        }

        OfferStatus currentStatus = offer.getStatus();

        // PENDING → ACCEPTED / DECLINED
        if (currentStatus != OfferStatus.PENDING) {
            throw new BusinessException(BAD_REQUEST, "只能对「待接受」的 Offer 进行状态变更");
        }
        if (newStatus == OfferStatus.PENDING) {
            throw new BusinessException(BAD_REQUEST, "不能变更为「待接受」，该状态是初始状态");
        }

        offer.setStatus(newStatus);
        offerMapper.updateById(offer);
        return offer;
    }
}
