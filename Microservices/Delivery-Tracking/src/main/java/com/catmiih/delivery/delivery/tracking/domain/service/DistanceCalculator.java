package com.catmiih.delivery.delivery.tracking.domain.service;

import com.catmiih.delivery.delivery.tracking.domain.model.Coordinates;

public final class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private DistanceCalculator() {}

    public static double calculate(
            Coordinates origin,
            Coordinates destination
    ) {
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

        return EARTH_RADIUS_KM * c;
    }
}
