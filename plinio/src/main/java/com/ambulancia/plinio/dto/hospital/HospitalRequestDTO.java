package com.ambulancia.plinio.dto.hospital;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HospitalRequestDTO(

        @NotBlank(message = "Nome do hospital é obrigatório")
        @Size(max = 100)
        String name,

        boolean available,

        @Min(value = 0, message = "Número de camas não pode ser negativo")
        int totalBeds,

        @Min(value = 0, message = "Leitos ocupados não pode ser negativo")
        int totalOccupiedBeds,

        @NotNull(message = "Endereço é obrigatório")
        Long addressId
) {
}
