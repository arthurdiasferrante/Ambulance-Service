package com.ambulancia.plinio.controller;

import com.ambulancia.plinio.dto.address.AddressRequestDTO;
import com.ambulancia.plinio.dto.address.AddressResponseDTO;
import com.ambulancia.plinio.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController {

    private final AddressService service;

    public AddressController(AddressService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(@Valid @RequestBody AddressRequestDTO requestDTO) {
        AddressResponseDTO response = service.createAddress(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> listAddresses() {
        return ResponseEntity.ok(service.listAddresses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> getAddress(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAddress(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable Long id, @Valid @RequestBody AddressRequestDTO requestDTO) {
        return ResponseEntity.ok(service.updateAddress(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        service.deleteAddress(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}