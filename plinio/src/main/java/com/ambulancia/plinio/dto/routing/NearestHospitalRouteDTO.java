package com.ambulancia.plinio.dto.routing;

import com.ambulancia.plinio.dto.hospital.HospitalResponseDTO;

import java.util.List;

public record NearestHospitalRouteDTO(
        List<Long> routeAddressIds,
        HospitalResponseDTO hospital,
        /** Comprimento total da rota no plano (soma dos pesos das arestas). */
        double totalRouteDistance) {
}
