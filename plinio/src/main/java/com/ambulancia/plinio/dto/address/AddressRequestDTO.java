package com.ambulancia.plinio.dto.address;

import jakarta.validation.constraints.NotNull;

public record AddressRequestDTO (

        @NotNull(message = "Bairro é obrigatório")
        String name) {
}
