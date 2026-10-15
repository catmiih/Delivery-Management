package com.catmiih.delivery.delivery.tracking.domain.service;

import com.catmiih.delivery.delivery.tracking.domain.model.ContactPoint;

public interface DeliveryTimeEstimationService {
    DeliveryEstimate estimate(ContactPoint sender, ContactPoint receiver);
}
