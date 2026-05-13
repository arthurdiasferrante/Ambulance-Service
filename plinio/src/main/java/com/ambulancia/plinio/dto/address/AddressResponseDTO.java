package com.ambulancia.plinio.dto.address;

import org.antlr.v4.runtime.misc.NotNull;

public record AddressResponseDTO(
        Long id,
        String neighborhood
    ) {
}
