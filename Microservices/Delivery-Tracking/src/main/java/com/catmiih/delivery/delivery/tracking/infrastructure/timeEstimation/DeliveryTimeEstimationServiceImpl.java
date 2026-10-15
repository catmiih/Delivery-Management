package com.catmiih.delivery.delivery.tracking.infrastructure.timeEstimation;

import com.catmiih.delivery.delivery.tracking.domain.model.ContactPoint;
import com.catmiih.delivery.delivery.tracking.domain.model.Coordinates;
import com.catmiih.delivery.delivery.tracking.domain.service.DeliveryEstimate;
import com.catmiih.delivery.delivery.tracking.domain.service.DeliveryTimeEstimationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DeliveryTimeEstimationServiceImpl
        implements DeliveryTimeEstimationService {

    private final GeocodingService geocodingService;
    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public DeliveryEstimate estimate(ContactPoint sender, ContactPoint recipient) {


        Coordinates origin = geocodingService.geocode(
                sender.getZipCode(),
                sender.getStreet(),
                sender.getNumber()
        );
        Coordinates destination = geocodingService.geocode(
                recipient.getZipCode(),
                recipient.getStreet(),
                recipient.getNumber()
        );

        double lat1 = Math.toRadians(origin.latitude());
        double lat2 = Math.toRadians(destination.latitude());

        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(
                destination.longitude() - origin.longitude()
        );

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(deltaLon / 2), 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        double distanceKm = EARTH_RADIUS_KM * c;

        return new DeliveryEstimate(estimateDeliveryTime(distanceKm),distanceKm);
    }

    private Duration estimateDeliveryTime(double distanceKm) {
        double estimatedRoadDistanceKm = distanceKm * 1.3;

        double averageSpeedKmh = 25.0;
        long preparationMinutes = 10;

        long travelMinutes = (long) Math.ceil(
                estimatedRoadDistanceKm / averageSpeedKmh * 60
        );

        return Duration.ofMinutes(
                preparationMinutes + travelMinutes
        );
    }
}
