package com.offerpilot.application.controller.internal;

import com.offerpilot.api.dto.OfferDTO;
import com.offerpilot.application.entity.Offer;
import com.offerpilot.application.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部接口 —— 供 AI 服务通过 Feign 获取 Offer
 */
@RestController
@RequestMapping("/internal/offers")
@RequiredArgsConstructor
public class OfferInternalController {

    private final OfferService offerService;

    /**
     * 获取某投递的 Offer
     */
    @GetMapping
    public OfferDTO getOffer(@RequestParam Long applicationId) {
        Offer offer = offerService.findByApplicationId(applicationId);
        if (offer == null) {
            return null;
        }
        return toDTO(offer);
    }

    private OfferDTO toDTO(Offer o) {
        return OfferDTO.builder()
                .id(o.getId())
                .applicationId(o.getApplicationId())
                .salary(o.getSalary())
                .bonus(o.getBonus())
                .stock(o.getStock())
                .benefits(o.getBenefits())
                .deadline(o.getDeadline())
                .status(o.getStatus() != null ? o.getStatus().name() : null)
                .remark(o.getRemark())
                .build();
    }
}
