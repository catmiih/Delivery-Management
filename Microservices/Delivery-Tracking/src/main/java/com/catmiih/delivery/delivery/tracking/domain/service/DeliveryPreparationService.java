package com.catmiih.delivery.delivery.tracking.domain.service;

import com.catmiih.delivery.delivery.tracking.api.model.ContactPointInput;
import com.catmiih.delivery.delivery.tracking.api.model.DeliveryInput;
import com.catmiih.delivery.delivery.tracking.api.model.ItemInput;
import com.catmiih.delivery.delivery.tracking.domain.exception.DomainException;
import com.catmiih.delivery.delivery.tracking.domain.model.ContactPoint;
import com.catmiih.delivery.delivery.tracking.domain.model.Delivery;
import com.catmiih.delivery.delivery.tracking.domain.repository.DeliveryRepository;
import com.catmiih.delivery.delivery.tracking.infrastructure.timeEstimation.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryPreparationService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryTimeEstimationService deliveryTimeEstimationService;
    private final CourierPayoutCalculationService courierPayoutCalculationService;

    @Transactional
    public Delivery draft(DeliveryInput input){
        Delivery delivery = Delivery.draft();

        handlePreparation(input,delivery);
        return deliveryRepository.saveAndFlush(delivery);
    }

    @Transactional
    public Delivery edit(UUID deliveryId, DeliveryInput input){
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DomainException());

        delivery.removeItems();
        handlePreparation(input,delivery);
        return deliveryRepository.saveAndFlush(delivery);
    }

    private void handlePreparation(DeliveryInput input, Delivery delivery) {
        ContactPointInput senderInput = input.getSender();
        ContactPointInput recipientInput = input.getRecipient();

        ContactPoint sender = ContactPoint.builder()
                .zipCode(senderInput.getZipCode())
                .street(senderInput.getStreet())
                .number(senderInput.getNumber())
                .complement(senderInput.getComplement())
                .name(senderInput.getName())
                .phone(senderInput.getPhone())
                .build();

        ContactPoint recipient = ContactPoint.builder()
                .zipCode(recipientInput.getZipCode())
                .street(recipientInput.getStreet())
                .number(recipientInput.getNumber())
                .complement(recipientInput.getComplement())
                .name(recipientInput.getName())
                .phone(recipientInput.getPhone())
                .build();

        DeliveryEstimate estimate = deliveryTimeEstimationService.estimate(sender, recipient);
        BigDecimal payout = courierPayoutCalculationService.calculatePayout(estimate.getDistanceInKm());

        BigDecimal distanceFee = calculateFee(estimate.getDistanceInKm());

        Delivery.PreparationDetails preparationDetails = Delivery.PreparationDetails.builder()
                .recipient(recipient)
                .sender(sender)
                .expectedDeliveryTime(estimate.getEstimatedTime())
                .courierPayout(payout)
                .distanceFee(distanceFee)
                .build();

        delivery.editPreparationDetails(preparationDetails);
        for (ItemInput item : input.getItems()) {
            delivery.addItem(item.getName(),item.getQuantity());
        }
    }

    private BigDecimal calculateFee(Double distanceInKm) {
        return new BigDecimal("3")
                .multiply(new BigDecimal(distanceInKm))
                .setScale(2,RoundingMode.HALF_EVEN);
    }
}
