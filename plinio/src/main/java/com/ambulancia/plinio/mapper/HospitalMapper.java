package com.ambulancia.plinio.mapper;

import com.ambulancia.plinio.dto.hospital.HospitalRequestDTO;
import com.ambulancia.plinio.dto.hospital.HospitalResponseDTO;
import com.ambulancia.plinio.model.Hospital;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HospitalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "address", ignore = true)
    Hospital toEntity(HospitalRequestDTO requestDTO);

    @Mapping(source = "address.neighborhood", target = "addressName")
    HospitalResponseDTO toResponseDTO(Hospital hospital);

    List<HospitalResponseDTO> toResponseDTOList(List<Hospital> hospitals);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "address", ignore = true)
    void updateEntityFromDTO(HospitalRequestDTO requestDTO, @MappingTarget Hospital entity);
}
