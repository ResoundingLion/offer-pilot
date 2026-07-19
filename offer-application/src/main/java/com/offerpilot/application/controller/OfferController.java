package com.offerpilot.application.controller;

import com.offerpilot.application.converter.OfferConverter;
import com.offerpilot.application.dto.OfferCreateRequest;
import com.offerpilot.application.dto.OfferUpdateRequest;
import com.offerpilot.application.dto.StatusUpdateRequest;
import com.offerpilot.application.entity.Offer;
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
        return Result.success(OfferConverter.convertToVO(offer));
    }

    /**
     * 新增 Offer
     * applicationId 从路径取，创建时默认状态 PENDING
     */
    @PostMapping("/api/applications/{appId}/offers")
    public Result<OfferVO> createOffer(@PathVariable Long appId, @Valid @RequestBody OfferCreateRequest request) {
        Offer offer = OfferConverter.convertToEntity(appId, request);
        Offer created = offerService.create(offer);
        return Result.created(OfferConverter.convertToVO(created));
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

        Offer offer = OfferConverter.convertToEntity(request);
        Offer updated = offerService.update(offer);
        return Result.success(OfferConverter.convertToVO(updated));
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
        return Result.success(OfferConverter.convertToVO(updated));
    }
}
