package com.ambulancia.plinio.dto.graph;

import jakarta.validation.constraints.NotNull;

public record GraphEdgeRequestDTO(
        @NotNull(message = "fromAddressId é obrigatório")
        Long fromAddressId,
        @NotNull(message = "toAddressId é obrigatório")
        Long toAddressId
) {
}
