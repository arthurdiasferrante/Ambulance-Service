package com.ambulancia.plinio.service;

import com.ambulancia.plinio.dto.hospital.HospitalRequestDTO;
import com.ambulancia.plinio.dto.hospital.HospitalResponseDTO;
import com.ambulancia.plinio.mapper.HospitalMapper;
import com.ambulancia.plinio.model.Address;
import com.ambulancia.plinio.model.Hospital;
import com.ambulancia.plinio.repository.AddressRepository;
import com.ambulancia.plinio.repository.HospitalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final AddressRepository addressRepository;
    private final HospitalMapper mapper;

    public HospitalService(HospitalRepository hospitalRepository, AddressRepository addressRepository, HospitalMapper hospitalMapper) {
        this.hospitalRepository = hospitalRepository;
        this.addressRepository = addressRepository;
        this.mapper = hospitalMapper;
    }

    public HospitalResponseDTO createHospital(HospitalRequestDTO requestDTO) {
        Address address = addressRepository
                .findById(requestDTO.addressId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado"));
        Hospital hospital = mapper.toEntity(requestDTO);
        hospital.setAddress(address);
        Hospital saved = hospitalRepository.save(hospital);
        return mapper.toResponseDTO(saved);
    }

    public HospitalResponseDTO getHospital(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital não encontrado"));
        return mapper.toResponseDTO(hospital);
    }

    public List<HospitalResponseDTO> listHospitals() {
        return mapper.toResponseDTOList(hospitalRepository.findAll());
    }

    public HospitalResponseDTO updateHospital(Long id, HospitalRequestDTO requestDTO) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital não encontrado"));
        mapper.updateEntityFromDTO(requestDTO, hospital);
        Hospital saved = hospitalRepository.save(hospital);
        return mapper.toResponseDTO(saved);
    }

    public HospitalResponseDTO updateHospitalAddress(Long id, HospitalRequestDTO requestDTO) {
        Address address = addressRepository
                .findById(requestDTO.addressId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado"));
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital não encontrado"));
        hospital.setAddress(address);
        Hospital saved = hospitalRepository.save(hospital);
        return mapper.toResponseDTO(saved);
    }

    public void deleteHospital(Long id) {
        Hospital hospital = hospitalRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hospital não encontrado"));
        hospitalRepository.deleteById(id);
    }
}
