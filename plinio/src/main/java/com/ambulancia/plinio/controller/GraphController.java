package com.ambulancia.plinio.controller;

import com.ambulancia.plinio.dto.graph.GraphEdgeRequestDTO;
import com.ambulancia.plinio.dto.graph.GraphEdgeResponseDTO;
import com.ambulancia.plinio.model.GraphEdge;
import com.ambulancia.plinio.service.GraphService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/graph")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping("/edges")
    public ResponseEntity<List<GraphEdgeResponseDTO>> listEdges() {
        List<GraphEdgeResponseDTO> edges =
                graphService.listEdges().stream().map(GraphController::toResponse).toList();
        return ResponseEntity.ok(edges);
    }

    @PostMapping("/edges")
    public ResponseEntity<GraphEdgeResponseDTO> addEdge(@Valid @RequestBody GraphEdgeRequestDTO body) {
        Optional<GraphEdge> created =
                graphService.addBidirectionalEdge(body.fromAddressId(), body.toAddressId());
        if (created.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created.get()));
        }
        GraphEdge existing =
                graphService
                        .findEdgeByEndpoints(body.fromAddressId(), body.toAddressId())
                        .orElseThrow();
        return ResponseEntity.status(HttpStatus.OK).body(toResponse(existing));
    }

    private static GraphEdgeResponseDTO toResponse(GraphEdge e) {
        return new GraphEdgeResponseDTO(e.getId(), e.getAddressAId(), e.getAddressBId());
    }
}
