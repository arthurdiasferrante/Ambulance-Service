package com.ambulancia.plinio.service;

import com.ambulancia.plinio.model.Hospital;
import com.ambulancia.plinio.repository.AddressRepository;
import com.ambulancia.plinio.repository.HospitalRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class GraphService {

    private final Map<Long, List<Long>> adjacencyList = new ConcurrentHashMap<>();

    private final Map<Long, List<Hospital>> hospitalByAddressId = new ConcurrentHashMap<>();

    private final AddressRepository addressRepository;
    private final HospitalRepository hospitalRepository;

    public GraphService(AddressRepository addressRepository, HospitalRepository hospitalRepository) {
        this.addressRepository = addressRepository;
        this.hospitalRepository = hospitalRepository;
    }

    @PostConstruct
    public void buildGraphInMemory() {

    }

    public void addConnectionInMemory(Long addressId, Long adjacentId) {
        adjacencyList.computeIfAbsent(addressId, k -> new ArrayList<>()).add(adjacentId);
        adjacencyList.computeIfAbsent(adjacentId, k -> new ArrayList<>()).add(addressId); // Se for mão dupla
    }

    public Hospital findNearestAvailableHospital(Long currentAddressId) {
        return null;
    }

}
