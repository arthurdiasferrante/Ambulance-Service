package com.ambulancia.plinio.service;

import com.ambulancia.plinio.dto.address.AddressRequestDTO;
import com.ambulancia.plinio.dto.address.AddressResponseDTO;
import com.ambulancia.plinio.mapper.AddressMapper;
import com.ambulancia.plinio.model.Address;
import com.ambulancia.plinio.repository.AddressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository repository;
    private final AddressMapper mapper;
    private final GraphService graphService;

    public AddressService(
            AddressRepository addressRepository, AddressMapper addressMapper, GraphService graphService) {
        this.repository = addressRepository;
        this.mapper = addressMapper;
        this.graphService = graphService;
    }

    public AddressResponseDTO createAddress(AddressRequestDTO requestDTO) {
        Address address = mapper.toEntity(requestDTO);
        Address saved = repository.save(address);
        graphService.refreshFromDatabase();
        return mapper.toResponseDTO(saved);
    }

    public AddressResponseDTO getAddress(Long id) {
        Address address = repository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado: " + id));
        return mapper.toResponseDTO(address);
    }

    public List<AddressResponseDTO> listAddresses() {
        return mapper.toResponseDTOList(repository.findAll());
    }

    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO requestDTO) {
        Address address = repository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado: " + id));
        mapper.updateEntityFromDTO(requestDTO, address);
        Address saved = repository.save(address);
        graphService.refreshFromDatabase();
        return mapper.toResponseDTO(saved);
    }

    public void deleteAddress(Long id) {
        Address address = repository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado: " + id));
        graphService.deleteEdgesIncidentTo(id);
        repository.delete(address);
        graphService.refreshFromDatabase();
    }
}
