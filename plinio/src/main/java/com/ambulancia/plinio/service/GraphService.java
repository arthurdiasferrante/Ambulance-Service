package com.ambulancia.plinio.service;

import com.ambulancia.plinio.model.Address;
import com.ambulancia.plinio.model.GraphEdge;
import com.ambulancia.plinio.model.Hospital;
import com.ambulancia.plinio.repository.AddressRepository;
import com.ambulancia.plinio.repository.GraphEdgeRepository;
import com.ambulancia.plinio.repository.HospitalRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GraphService {

    private final Map<Long, List<Long>> adjacencyList = new ConcurrentHashMap<>();
    private final Map<Long, List<Hospital>> hospitalByAddressId = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> addressAvailable = new ConcurrentHashMap<>();

    private final AddressRepository addressRepository;
    private final HospitalRepository hospitalRepository;
    private final GraphEdgeRepository graphEdgeRepository;

    public GraphService(
            AddressRepository addressRepository,
            HospitalRepository hospitalRepository,
            GraphEdgeRepository graphEdgeRepository) {
        this.addressRepository = addressRepository;
        this.hospitalRepository = hospitalRepository;
        this.graphEdgeRepository = graphEdgeRepository;
    }

    @PostConstruct
    public void buildGraphInMemory() {
        refreshFromDatabase();
    }

    @Transactional(readOnly = true)
    public synchronized void refreshFromDatabase() {
        adjacencyList.clear();
        hospitalByAddressId.clear();
        addressAvailable.clear();

        for (GraphEdge edge : graphEdgeRepository.findAll()) {
            linkUndirected(edge.getAddressAId(), edge.getAddressBId());
        }

        for (Address address : addressRepository.findAll()) {
            addressAvailable.put(address.getId(), address.isAvailable());
        }

        for (Hospital hospital : hospitalRepository.findAll()) {
            long addressId = hospital.getAddress().getId();
            hospitalByAddressId
                    .computeIfAbsent(addressId, k -> new ArrayList<>())
                    .add(hospital);
        }
    }

    @Transactional
    public synchronized void addBidirectionalEdge(long addressIdA, long addressIdB) {
        if (addressIdA == addressIdB) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um endereço não pode ligar a ele mesmo");
        }
        if (!addressRepository.existsById(addressIdA) || !addressRepository.existsById(addressIdB)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado");
        }

        long lo = Math.min(addressIdA, addressIdB);
        long hi = Math.max(addressIdA, addressIdB);

        if (graphEdgeRepository.existsByAddressAIdAndAddressBId(lo, hi)) {
            return;
        }

        GraphEdge edge = new GraphEdge();
        edge.setAddressAId(lo);
        edge.setAddressBId(hi);
        graphEdgeRepository.save(edge);
        linkUndirected(lo, hi);
    }

    @Transactional
    public synchronized void deleteEdgesIncidentTo(long addressId) {
        graphEdgeRepository.deleteAllIncidentTo(addressId);
        refreshFromDatabase();
    }

    public Optional<NearestHospitalRoutingResult> findNearestAvailableHospitalRoute(long fromAddressId) {
        Address origin = addressRepository
                .findById(fromAddressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado"));

        if (!origin.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Endereço de origem indisponível — não é possível calcular rota a partir deste vértice");
        }

        if (isDestinationWithAvailableHospital(fromAddressId)) {
            Hospital pick = firstAvailableHospitalAt(fromAddressId);
            if (pick != null) {
                return Optional.of(new NearestHospitalRoutingResult(pick, List.of(fromAddressId)));
            }
        }

        Map<Long, Long> parent = new HashMap<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();

        parent.put(fromAddressId, null);
        queue.add(fromAddressId);

        while (!queue.isEmpty()) {
            long current = queue.removeFirst();

            for (long neighbor : neighborsOf(current)) {
                if (!Boolean.TRUE.equals(addressAvailable.get(neighbor))) {
                    continue;
                }
                if (parent.containsKey(neighbor)) {
                    continue;
                }
                parent.put(neighbor, current);

                if (isDestinationWithAvailableHospital(neighbor)) {
                    Hospital hospital = firstAvailableHospitalAt(neighbor);
                    if (hospital != null) {
                        List<Long> path = reconstructPath(parent, fromAddressId, neighbor);
                        return Optional.of(new NearestHospitalRoutingResult(hospital, path));
                    }
                }

                queue.add(neighbor);
            }
        }

        return Optional.empty();
    }

    public List<GraphEdge> listEdges() {
        return graphEdgeRepository.findAll();
    }

    private List<Long> neighborsOf(long addressId) {
        return adjacencyList.getOrDefault(addressId, List.of());
    }

    private void linkUndirected(long a, long b) {
        adjacencyList.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
        adjacencyList.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
    }

    private boolean isDestinationWithAvailableHospital(long addressId) {
        return hospitalByAddressId.getOrDefault(addressId, List.of()).stream().anyMatch(Hospital::isAvailable);
    }

    private Hospital firstAvailableHospitalAt(long addressId) {
        return hospitalByAddressId.getOrDefault(addressId, List.of()).stream()
                .filter(Hospital::isAvailable)
                .findFirst()
                .orElse(null);
    }

    private static List<Long> reconstructPath(Map<Long, Long> parent, long start, long end) {
        List<Long> backwards = new ArrayList<>();
        Long step = end;
        while (step != null) {
            backwards.add(step);
            step = parent.get(step);
        }
        Collections.reverse(backwards);
        return backwards;
    }
}
