package com.offerpilot.application.controller;

import com.offerpilot.application.dto.OfferCreateRequest;
import com.offerpilot.application.dto.OfferUpdateRequest;
import com.offerpilot.application.dto.StatusUpdateRequest;
import com.offerpilot.application.entity.Offer;
import com.offerpilot.application.enums.OfferStatus;
import com.offerpilot.application.service.OfferService;
import com.offerpilot.application.vo.OfferVO;
import com.offerpilot.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    /**
     * 获取指定投递的 Offer
     * 一个投递最多一个 Offer，所以返回单个对象
     */
    @GetMapping("/api/applications/{appId}/offers")
    public Result<OfferVO> getOfferByApplicationId(@PathVariable Long appId) {
        Offer offer = offerService.findByApplicationId(appId);
        if (offer == null) {
            return Result.notFound();
        }
        return Result.success(convertToVO(offer));
    }

    /**
     * 新增 Offer
     * applicationId 从路径取，创建时默认状态 PENDING
     */
    @PostMapping("/api/applications/{appId}/offers")
    public Result<OfferVO> createOffer(@PathVariable Long appId, @Valid @RequestBody OfferCreateRequest request) {
        Offer offer = new Offer();
        offer.setApplicationId(appId);
        offer.setSalary(request.getSalary());
        offer.setBonus(request.getBonus());
        offer.setStock(request.getStock());
        offer.setBenefits(request.getBenefits());
        offer.setDeadline(request.getDeadline());
        offer.setRemark(request.getRemark());
        offer.setStatus(OfferStatus.PENDING);

        Offer created = offerService.create(offer);
        return Result.created(convertToVO(created));
    }

    /**
     * 更新 Offer
     */
    @PutMapping("/api/offers/{id}")
    public Result<OfferVO> updateOffer(@PathVariable Long id, @Valid @RequestBody OfferUpdateRequest request) {
        Offer existing = offerService.findById(id);
        if (existing == null) {
            return Result.notFound();
        }

        Offer offer = new Offer();
        offer.setId(id);
        offer.setSalary(request.getSalary());
        offer.setBonus(request.getBonus());
        offer.setStock(request.getStock());
        offer.setBenefits(request.getBenefits());
        offer.setDeadline(request.getDeadline());
        offer.setRemark(request.getRemark());
        // status 不更新——状态变更走 PATCH

        Offer updated = offerService.update(offer);
        return Result.success(convertToVO(updated));
    }

    /**
     * 接受/拒绝 Offer
     * PENDING → ACCEPTED / DECLINED
     */
    @PatchMapping("/api/offers/{id}/status")
    public Result<OfferVO> updateOfferStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        Offer updated = offerService.updateStatus(id, request.getStatus());
        if (updated == null) {
            return Result.notFound();
        }
        return Result.success(convertToVO(updated));
    }

    private OfferVO convertToVO(Offer offer) {
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
}
