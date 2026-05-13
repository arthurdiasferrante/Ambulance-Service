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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GraphService {

    private static final double DIST_EPS = 1e-9;

    private final Map<Long, List<Long>> adjacencyList = new ConcurrentHashMap<>();
    private final Map<Long, List<Hospital>> hospitalByAddressId = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> addressAvailable = new ConcurrentHashMap<>();
    private final Map<Long, double[]> addressCoords = new ConcurrentHashMap<>();

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
        addressCoords.clear();

        for (GraphEdge edge : graphEdgeRepository.findAll()) {
            linkUndirected(edge.getAddressAId(), edge.getAddressBId());
        }

        for (Address address : addressRepository.findAll()) {
            addressAvailable.put(address.getId(), address.isAvailable());
            addressCoords.put(address.getId(), new double[] {address.getCoordX(), address.getCoordY()});
        }

        for (Hospital hospital : hospitalRepository.findAll()) {
            long addressId = hospital.getAddress().getId();
            hospitalByAddressId
                    .computeIfAbsent(addressId, k -> new ArrayList<>())
                    .add(hospital);
        }
    }

    @Transactional
    public synchronized Optional<GraphEdge> addBidirectionalEdge(long addressIdA, long addressIdB) {
        if (addressIdA == addressIdB) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um endereço não pode ligar a ele mesmo");
        }
        if (!addressRepository.existsById(addressIdA) || !addressRepository.existsById(addressIdB)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado");
        }

        long lo = Math.min(addressIdA, addressIdB);
        long hi = Math.max(addressIdA, addressIdB);

        if (graphEdgeRepository.existsByAddressAIdAndAddressBId(lo, hi)) {
            return Optional.empty();
        }

        GraphEdge edge = new GraphEdge();
        edge.setAddressAId(lo);
        edge.setAddressBId(hi);
        GraphEdge saved = graphEdgeRepository.save(edge);
        refreshFromDatabase();
        return Optional.of(saved);
    }

    @Transactional(readOnly = true)
    public Optional<GraphEdge> findEdgeByEndpoints(long addressIdA, long addressIdB) {
        long lo = Math.min(addressIdA, addressIdB);
        long hi = Math.max(addressIdA, addressIdB);
        return graphEdgeRepository.findByAddressAIdAndAddressBId(lo, hi);
    }

    @Transactional
    public synchronized void deleteEdgesIncidentTo(long addressId) {
        graphEdgeRepository.deleteAllIncidentTo(addressId);
    }


    public Optional<NearestHospitalRoutingResult> findNearestAvailableHospitalRoute(long fromAddressId) {
        Address origin = addressRepository
                .findById(fromAddressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado"));

        if (!origin.isAvailable()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Endereço de origem indisponível. Não é possível calcular rota a partir deste vértice");
        }

        if (isDestinationWithRoutableHospital(fromAddressId)) {
            Hospital pick = firstRoutableHospitalAt(fromAddressId);
            if (pick != null) {
                return Optional.of(new NearestHospitalRoutingResult(pick, List.of(fromAddressId), 0.0));
            }
        }

        Map<Long, Double> dist = new HashMap<>();
        Map<Long, Long> parent = new HashMap<>();
        parent.put(fromAddressId, null);
        dist.put(fromAddressId, 0.0);

        PriorityQueue<DijkstraNode> pq = new PriorityQueue<>();
        pq.add(new DijkstraNode(fromAddressId, 0.0));

        while (!pq.isEmpty()) {
            DijkstraNode cur = pq.poll();
            Double recorded = dist.get(cur.id);
            if (recorded == null || cur.dist > recorded + DIST_EPS) {
                continue;
            }

            if (cur.id != fromAddressId && isDestinationWithRoutableHospital(cur.id)) {
                Hospital hospital = firstRoutableHospitalAt(cur.id);
                if (hospital != null) {
                    List<Long> path = reconstructPath(parent, fromAddressId, cur.id);
                    return Optional.of(new NearestHospitalRoutingResult(hospital, path, cur.dist));
                }
            }

            for (long neighbor : neighborsOf(cur.id)) {
                if (!Boolean.TRUE.equals(addressAvailable.get(neighbor))) {
                    continue;
                }
                double w = edgeWeight(cur.id, neighbor);
                double nd = cur.dist + w;
                Double prev = dist.get(neighbor);
                if (prev == null || nd < prev - DIST_EPS) {
                    dist.put(neighbor, nd);
                    parent.put(neighbor, cur.id);
                    pq.add(new DijkstraNode(neighbor, nd));
                }
            }
        }

        return fallbackStraightLineNearestRoutableHospital(fromAddressId);
    }

    /**
     * When the road graph has no path to any hospital with vacancy, pick the geographically closest
     * routable hospital (Euclidean distance in the coordinate plane). Route is {@code [origin, hospitalAddress]}
     * even if there is no {@link GraphEdge} between them — callers may treat it as crow-fly / dispatch hint.
     */
    private Optional<NearestHospitalRoutingResult> fallbackStraightLineNearestRoutableHospital(long fromAddressId) {
        double[] origin = addressCoords.get(fromAddressId);
        if (origin == null) {
            return Optional.empty();
        }
        Hospital bestHospital = null;
        double bestDist = Double.POSITIVE_INFINITY;
        long bestHospitalAddressId = -1L;
        for (Hospital h : hospitalRepository.findAll()) {
            if (!isHospitalRoutable(h)) {
                continue;
            }
            long aid = h.getAddress().getId();
            double[] c = addressCoords.get(aid);
            if (c == null) {
                continue;
            }
            double d = Math.hypot(origin[0] - c[0], origin[1] - c[1]);
            if (d < bestDist - DIST_EPS
                    || (Math.abs(d - bestDist) <= DIST_EPS
                            && bestHospital != null
                            && h.getId() < bestHospital.getId())) {
                bestDist = d;
                bestHospital = h;
                bestHospitalAddressId = aid;
            } else if (bestHospital == null || d < bestDist - DIST_EPS) {
                bestDist = d;
                bestHospital = h;
                bestHospitalAddressId = aid;
            }
        }
        if (bestHospital == null) {
            return Optional.empty();
        }
        List<Long> path =
                fromAddressId == bestHospitalAddressId
                        ? List.of(fromAddressId)
                        : List.of(fromAddressId, bestHospitalAddressId);
        return Optional.of(new NearestHospitalRoutingResult(bestHospital, path, bestDist));
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

    private double edgeWeight(long fromId, long toId) {
        double[] a = addressCoords.get(fromId);
        double[] b = addressCoords.get(toId);
        if (a == null || b == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Coordenadas não carregadas para endereço " + fromId + " ou " + toId);
        }
        return Math.hypot(a[0] - b[0], a[1] - b[1]);
    }

    private static boolean isHospitalRoutable(Hospital h) {
        if (!h.isAvailable()) {
            return false;
        }
        int capacity = h.getTotalBeds();
        if (capacity <= 0) {
            return false;
        }
        return h.getTotalOccupiedBeds() < capacity;
    }

    private boolean isDestinationWithRoutableHospital(long addressId) {
        return hospitalByAddressId.getOrDefault(addressId, List.of()).stream().anyMatch(GraphService::isHospitalRoutable);
    }

    private Hospital firstRoutableHospitalAt(long addressId) {
        return hospitalByAddressId.getOrDefault(addressId, List.of()).stream()
                .filter(GraphService::isHospitalRoutable)
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

    private record DijkstraNode(long id, double dist) implements Comparable<DijkstraNode> {
        @Override
        public int compareTo(DijkstraNode o) {
            int c = Double.compare(dist, o.dist);
            if (c != 0) {
                return c;
            }
            return Long.compare(id, o.id);
        }
    }
}
