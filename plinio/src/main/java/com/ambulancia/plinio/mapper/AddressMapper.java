package com.ambulancia.plinio.mapper;

import com.ambulancia.plinio.dto.address.AddressRequestDTO;
import com.ambulancia.plinio.dto.address.AddressResponseDTO;
import com.ambulancia.plinio.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "available", ignore = true)
    @Mapping(source = "name", target = "neighborhood")
    Address toEntity(AddressRequestDTO requestDTO);

    AddressResponseDTO toResponseDTO(Address address);

    List<AddressResponseDTO> toResponseDTOList(List<Address> addresses);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "name", target = "neighborhood")
    void updateEntityFromDTO(AddressRequestDTO requestDTO, @MappingTarget Address entity);
}