package com.offerpilot.application.converter;

import com.offerpilot.application.dto.OfferCreateRequest;
import com.offerpilot.application.dto.OfferUpdateRequest;
import com.offerpilot.application.entity.Offer;
import com.offerpilot.application.enums.OfferStatus;
import com.offerpilot.application.vo.OfferVO;

public class OfferConverter {

    public static OfferVO convertToVO(Offer offer) {
        OfferVO vo = new OfferVO();
        vo.setId(offer.getId());
        vo.setApplicationId(offer.getApplicationId());
        vo.setSalary(offer.getSalary());
        vo.setBonus(offer.getBonus());
        vo.setStock(offer.getStock());
        vo.setBenefits(offer.getBenefits());
        vo.setDeadline(offer.getDeadline());
        vo.setStatus(offer.getStatus());
        vo.setRemark(offer.getRemark());
        vo.setCreatedAt(offer.getCreatedAt());
        vo.setUpdatedAt(offer.getUpdatedAt());
        return vo;
    }

    /**
     * @param applicationId 从路径 @PathVariable 传入
     * @param request       请求体 DTO
     */
    public static Offer convertToEntity(Long applicationId, OfferCreateRequest request) {
        Offer offer = new Offer();
        offer.setApplicationId(applicationId);
        offer.setSalary(request.getSalary());
        offer.setBonus(request.getBonus());
        offer.setStock(request.getStock());
        offer.setBenefits(request.getBenefits());
        offer.setDeadline(request.getDeadline());
        offer.setRemark(request.getRemark());
        // 新增 Offer 默认状态为 PENDING
        offer.setStatus(OfferStatus.PENDING);
        return offer;
    }

    public static Offer convertToEntity(OfferUpdateRequest request) {
        Offer offer = new Offer();
        offer.setId(request.getId());
        offer.setSalary(request.getSalary());
        offer.setBonus(request.getBonus());
        offer.setStock(request.getStock());
        offer.setBenefits(request.getBenefits());
        offer.setDeadline(request.getDeadline());
        offer.setRemark(request.getRemark());
        // status 不更新——状态变更走 PATCH
        return offer;
    }
}
