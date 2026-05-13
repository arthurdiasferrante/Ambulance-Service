package com.ambulancia.plinio.controller;

import com.ambulancia.plinio.dto.hospital.HospitalRequestDTO;
import com.ambulancia.plinio.dto.hospital.HospitalResponseDTO;
import com.ambulancia.plinio.service.HospitalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hospitals")
public class HospitalController {

    private final HospitalService service;

    public HospitalController(HospitalService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<HospitalResponseDTO> createHospital(@Valid @RequestBody HospitalRequestDTO requestDTO) {
        HospitalResponseDTO response = service.createHospital(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<HospitalResponseDTO>> listHospitals() {
        return ResponseEntity.ok(service.listHospitals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospitalResponseDTO> getHospital(@PathVariable Long id) {
        return ResponseEntity.ok(service.getHospital(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HospitalResponseDTO> updateHospital(@PathVariable Long id, @Valid @RequestBody HospitalRequestDTO requestDTO) {
        return ResponseEntity.ok(service.updateHospital(id, requestDTO));
    }

    @PatchMapping("/{id}/address")
    public ResponseEntity<HospitalResponseDTO> updateHospitalAddress(@PathVariable Long id, @Valid @RequestBody HospitalRequestDTO requestDTO) {
        return ResponseEntity.ok(service.updateHospitalAddress(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHospital(@PathVariable Long id) {
        service.deleteHospital(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
