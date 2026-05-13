package com.ambulancia.plinio.controller;

import com.ambulancia.plinio.dto.routing.NearestHospitalRouteDTO;
import com.ambulancia.plinio.service.RoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/routing")
public class RoutingController {

    private final RoutingService routingService;

    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @GetMapping("/from-address/{addressId}/nearest-hospital")
    public ResponseEntity<NearestHospitalRouteDTO> nearestHospital(@PathVariable long addressId) {
        return ResponseEntity.ok(routingService.findNearestAvailableHospitalRoute(addressId));
    }
}
