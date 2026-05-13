package com.ambulancia.plinio.dto.hospital;

public record HospitalResponseDTO (
        Long id,
        String name,
        boolean available,
        int totalBeds,
        int totalOccupiedBeds,
        String addressName
        ) {
}
