package com.ambulancia.plinio.dto.hospital;

import com.ambulancia.plinio.model.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HospitalRequestDTO(

        @NotNull(message = "Nome do hospital é obrigatório")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Deve ter um status")
        boolean available,

        @NotNull(message = "Número de camas é obrigatório")
        int totalBeds,

        @NotNull(message = "sei la caralho")
        int totalOccupiedBeds,

        @NotBlank
        Long addressId
        ) {
}
