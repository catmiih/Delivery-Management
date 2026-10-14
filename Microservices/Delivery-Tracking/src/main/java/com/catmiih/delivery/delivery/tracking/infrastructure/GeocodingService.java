package com.catmiih.delivery.delivery.tracking.infrastructure;

import com.catmiih.delivery.delivery.tracking.domain.model.Coordinates;

public interface GeocodingService {

    Coordinates geocode(
            String zipCode,
            String street,
            String number
    );
}

