package com.ambulancia.plinio.service;

import com.ambulancia.plinio.dto.routing.NearestHospitalRouteDTO;
import com.ambulancia.plinio.mapper.HospitalMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class RoutingService {

    private final GraphService graphService;
    private final HospitalMapper hospitalMapper;

    public RoutingService(GraphService graphService, HospitalMapper hospitalMapper) {
        this.graphService = graphService;
        this.hospitalMapper = hospitalMapper;
    }

    public NearestHospitalRouteDTO findNearestAvailableHospitalRoute(long fromAddressId) {
        Optional<NearestHospitalRoutingResult> result =
                graphService.findNearestAvailableHospitalRoute(fromAddressId);
        return result
                .map(
                        r -> new NearestHospitalRouteDTO(
                                r.routeAddressIds(),
                                hospitalMapper.toResponseDTO(r.hospital()),
                                r.totalRouteDistance()))
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Nenhum hospital com vaga alcançável a partir deste endereço"));
    }
}
