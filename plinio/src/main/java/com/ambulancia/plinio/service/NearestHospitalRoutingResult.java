package com.ambulancia.plinio.service;

import com.ambulancia.plinio.model.Hospital;

import java.util.List;

public record NearestHospitalRoutingResult(
        Hospital hospital,
        List<Long> routeAddressIds,
        double totalRouteDistance) {
}
