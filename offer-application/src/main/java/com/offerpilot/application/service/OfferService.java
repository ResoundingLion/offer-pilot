package com.offerpilot.application.service;

import com.offerpilot.application.entity.Offer;

import java.util.List;

public interface OfferService {
    Offer findById(Long id);

    List<Offer> findAll();

    Offer create(Offer offer);

    Offer update(Offer offer);

    void deleteById(Long id);

    Offer findByApplicationId(Long applicationId);

    Offer updateStatus(Long id, String status);
}
