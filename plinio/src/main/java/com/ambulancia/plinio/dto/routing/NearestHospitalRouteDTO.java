package com.ambulancia.plinio.dto.routing;

import com.ambulancia.plinio.dto.hospital.HospitalResponseDTO;

import java.util.List;

public record NearestHospitalRouteDTO(
        List<Long> routeAddressIds,
        HospitalResponseDTO hospital
) {
}
