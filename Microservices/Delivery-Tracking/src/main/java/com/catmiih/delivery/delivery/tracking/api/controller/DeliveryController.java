package com.catmiih.delivery.delivery.tracking.api.controller;

import com.catmiih.delivery.delivery.tracking.api.model.DeliveryInput;
import com.catmiih.delivery.delivery.tracking.domain.model.Delivery;
import com.catmiih.delivery.delivery.tracking.domain.service.DeliveryPreparationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryPreparationService deliveryPreparationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Delivery draft(@RequestBody @Valid DeliveryInput input){
        return deliveryPreparationService.draft(input);
    }

    @PutMapping("/{deliveryId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Delivery edit(@PathVariable UUID deliveryId,
                         @RequestBody @Valid DeliveryInput input){
        return deliveryPreparationService.edit(deliveryId, input);
    }
}
