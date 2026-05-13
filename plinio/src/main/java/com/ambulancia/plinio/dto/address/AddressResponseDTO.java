package com.ambulancia.plinio.dto.address;

public record AddressResponseDTO(
        Long id,
        String neighborhood,
        boolean available
) {
}

