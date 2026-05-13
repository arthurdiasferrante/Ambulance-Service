package com.ambulancia.plinio.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class NeighborhoodHospitalRouter {

    private final Set<String> neighborhoods = new HashSet<>();
    private final Map<String, Set<String>> adjacency = new HashMap<>();
    private final List<HospitalRegistration> hospitals = new ArrayList<>();

    public void addNeighborhood(String name) {
        Objects.requireNonNull(name, "neighborhood name");
        neighborhoods.add(name);
        adjacency.putIfAbsent(name, new TreeSet<>());
    }

    public void connectNeighborhoods(String a, String b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        if (!neighborhoods.contains(a) || !neighborhoods.contains(b)) {
            throw new IllegalArgumentException("Neighborhoods must exist: " + a + ", " + b);
        }
        adjacency.computeIfAbsent(a, k -> new TreeSet<>()).add(b);
        adjacency.computeIfAbsent(b, k -> new TreeSet<>()).add(a);
    }

    public void addHospital(String hospitalName, String neighborhood, int totalBeds, int occupiedBeds) {
        Objects.requireNonNull(hospitalName);
        Objects.requireNonNull(neighborhood);
        if (!neighborhoods.contains(neighborhood)) {
            throw new IllegalArgumentException("Unknown neighborhood: " + neighborhood);
        }
        if (totalBeds < 0 || occupiedBeds < 0 || occupiedBeds > totalBeds) {
            throw new IllegalArgumentException("Invalid bed counts for " + hospitalName);
        }
        hospitals.add(new HospitalRegistration(hospitalName, neighborhood, totalBeds, occupiedBeds));
    }

    public String findHospital(String originNeighborhood) {
        if (!neighborhoods.contains(originNeighborhood)) {
            throw new IllegalArgumentException("Unknown neighborhood: " + originNeighborhood);
        }

        String bestInOrigin = firstAvailableHospitalNameLexicographic(originNeighborhood);
        if (bestInOrigin != null) {
            return bestInOrigin;
        }

        Set<String> visited = new HashSet<>();
        List<String> frontier = new ArrayList<>();
        frontier.add(originNeighborhood);
        visited.add(originNeighborhood);

        while (!frontier.isEmpty()) {
            List<String> next = new ArrayList<>();

            for (String node : new TreeSet<>(frontier)) {
                for (String neighbor : adjacency.getOrDefault(node, Collections.emptySet())) {
                    if (visited.add(neighbor)) {
                        next.add(neighbor);
                    }
                }
            }

            String bestAtDistance = null;
            for (String node : new TreeSet<>(next)) {
                String candidate = firstAvailableHospitalNameLexicographic(node);
                if (candidate != null && (bestAtDistance == null || candidate.compareTo(bestAtDistance) < 0)) {
                    bestAtDistance = candidate;
                }
            }
            if (bestAtDistance != null) {
                return bestAtDistance;
            }

            frontier = next;
        }

        return "";
    }

    private String firstAvailableHospitalNameLexicographic(String neighborhood) {
        TreeSet<String> names = new TreeSet<>();
        for (HospitalRegistration h : hospitals) {
            if (h.neighborhood().equals(neighborhood) && h.occupiedBeds() < h.totalBeds()) {
                names.add(h.name());
            }
        }
        return names.isEmpty() ? null : names.first();
    }

    private record HospitalRegistration(String name, String neighborhood, int totalBeds, int occupiedBeds) {}
}
