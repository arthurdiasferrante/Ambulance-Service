package com.ambulancia.plinio.service;

import com.ambulancia.plinio.model.Address;
import com.ambulancia.plinio.model.GraphEdge;
import com.ambulancia.plinio.model.Hospital;
import com.ambulancia.plinio.repository.AddressRepository;
import com.ambulancia.plinio.repository.GraphEdgeRepository;
import com.ambulancia.plinio.repository.HospitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GraphServiceRoutingIntegrationTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private GraphEdgeRepository graphEdgeRepository;

    @Autowired
    private GraphService graphService;

    @Autowired
    private RoutingService routingService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetGraph() {
        hospitalRepository.deleteAll();
        graphEdgeRepository.deleteAll();
        addressRepository.deleteAll();
        graphService.refreshFromDatabase();
    }

    @Test
    void weightedRoute_prefersFartherVertex_whenCloserHospitalHasNoVacancy() {
        Address a = saveAddress("A", true, 0, 0);
        Address b = saveAddress("B", true, 10, 0);
        Address c = saveAddress("C", true, 20, 0);
        link(a.getId(), b.getId());
        link(b.getId(), c.getId());

        saveHospital("H-A", true, 10, 10, a);
        saveHospital("H-C", true, 10, 5, c);

        graphService.refreshFromDatabase();

        Optional<NearestHospitalRoutingResult> route = graphService.findNearestAvailableHospitalRoute(b.getId());
        assertThat(route).isPresent();
        assertThat(route.get().hospital().getName()).isEqualTo("H-C");
        assertThat(route.get().routeAddressIds()).containsExactly(b.getId(), c.getId());
        assertThat(route.get().totalRouteDistance()).isCloseTo(10.0, within(1e-6));
    }

    @Test
    void originWithRoutableHospital_returnsSingletonPath() {
        Address a = saveAddress("A", true, 5, 5);
        saveHospital("H-A", true, 10, 3, a);
        graphService.refreshFromDatabase();

        Optional<NearestHospitalRoutingResult> route = graphService.findNearestAvailableHospitalRoute(a.getId());
        assertThat(route).isPresent();
        assertThat(route.get().routeAddressIds()).containsExactly(a.getId());
        assertThat(route.get().hospital().getName()).isEqualTo("H-A");
        assertThat(route.get().totalRouteDistance()).isZero();
    }

    @Test
    void totalRouteDistance_matchesSumOfEuclideanLegs() {
        Address o = saveAddress("O", true, 0, 0);
        Address p = saveAddress("P", true, 3, 0);
        Address q = saveAddress("Q", true, 3, 4);
        link(o.getId(), p.getId());
        link(p.getId(), q.getId());
        saveHospital("H-Q", true, 5, 0, q);
        graphService.refreshFromDatabase();

        Optional<NearestHospitalRoutingResult> route = graphService.findNearestAvailableHospitalRoute(o.getId());
        assertThat(route).isPresent();
        assertThat(route.get().totalRouteDistance()).isCloseTo(7.0, within(1e-6));
    }

    @Test
    void skipsFullHospitalAtOriginWhenNeighborHasVacancy() {
        Address a = saveAddress("A", true, 0, 0);
        Address b = saveAddress("B", true, 1, 0);
        link(a.getId(), b.getId());
        saveHospital("H-A", true, 20, 20, a);
        saveHospital("H-B", true, 15, 10, b);
        graphService.refreshFromDatabase();

        Optional<NearestHospitalRoutingResult> route = graphService.findNearestAvailableHospitalRoute(a.getId());
        assertThat(route).isPresent();
        assertThat(route.get().hospital().getName()).isEqualTo("H-B");
        assertThat(route.get().routeAddressIds()).containsExactly(a.getId(), b.getId());
        assertThat(route.get().totalRouteDistance()).isCloseTo(1.0, within(1e-6));
    }

    @Test
    void skipsUnavailableAddressVertices() {
        Address a = saveAddress("A", true, 0, 0);
        Address b = saveAddress("B", false, 1, 0);
        Address c = saveAddress("C", true, 2, 0);
        link(a.getId(), b.getId());
        link(b.getId(), c.getId());
        saveHospital("H-C", true, 10, 0, c);
        graphService.refreshFromDatabase();

        Optional<NearestHospitalRoutingResult> route = graphService.findNearestAvailableHospitalRoute(a.getId());
        assertThat(route).isEmpty();
    }

    @Test
    void isolatedAddress_withoutGraphPath_usesStraightLineFallbackToNearestHospitalWithVacancy() {
        Address center = saveAddress("Center", true, 0, 0);
        Address hub = saveAddress("Hub", true, 50, 50);
        Address remote = saveAddress("Cantareira", true, 120, 200);
        link(center.getId(), hub.getId());
        saveHospital("H-Center", true, 20, 5, center);
        graphService.refreshFromDatabase();

        Optional<NearestHospitalRoutingResult> route = graphService.findNearestAvailableHospitalRoute(remote.getId());
        assertThat(route).isPresent();
        assertThat(route.get().hospital().getName()).isEqualTo("H-Center");
        assertThat(route.get().routeAddressIds()).containsExactly(remote.getId(), center.getId());
        assertThat(route.get().totalRouteDistance()).isCloseTo(Math.hypot(120, 200), within(1e-6));
    }

    @Test
    void routingService_throwsWhenNoReachableHospitalWithVacancy() {
        Address a = saveAddress("A", true, 0, 0);
        Address b = saveAddress("B", true, 1, 0);
        link(a.getId(), b.getId());
        saveHospital("H-B", true, 8, 8, b);
        graphService.refreshFromDatabase();

        assertThatThrownBy(() -> routingService.findNearestAvailableHospitalRoute(a.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(
                        ex -> {
                            HttpStatusCode code = ((ResponseStatusException) ex).getStatusCode();
                            assertThat(code.value()).isEqualTo(HttpStatus.NOT_FOUND.value());
                        });
    }

    @Test
    void adminClosedHospitalIgnoredEvenWithVacancy() {
        Address a = saveAddress("A", true, 0, 0);
        Address b = saveAddress("B", true, 5, 0);
        link(a.getId(), b.getId());
        saveHospital("H-A", false, 10, 0, a);
        saveHospital("H-B", true, 10, 5, b);
        graphService.refreshFromDatabase();

        Optional<NearestHospitalRoutingResult> route = graphService.findNearestAvailableHospitalRoute(a.getId());
        assertThat(route).isPresent();
        assertThat(route.get().hospital().getName()).isEqualTo("H-B");
    }

    @Test
    void httpNearestHospital_returnsJson() throws Exception {
        Address a = saveAddress("A", true, 0, 0);
        Address b = saveAddress("B", true, 10, 0);
        link(a.getId(), b.getId());
        saveHospital("H-B", true, 10, 2, b);
        graphService.refreshFromDatabase();

        mockMvc.perform(get("/routing/from-address/{id}/nearest-hospital", a.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeAddressIds[0]").value(a.getId()))
                .andExpect(jsonPath("$.routeAddressIds[1]").value(b.getId()))
                .andExpect(jsonPath("$.hospital.name").value("H-B"))
                .andExpect(jsonPath("$.totalRouteDistance").value(10.0));
    }

    private Address saveAddress(String neighborhood, boolean available, double x, double y) {
        Address address = new Address();
        address.setNeighborhood(neighborhood);
        address.setAvailable(available);
        address.setCoordX(x);
        address.setCoordY(y);
        return addressRepository.save(address);
    }

    private void link(long id1, long id2) {
        long lo = Math.min(id1, id2);
        long hi = Math.max(id1, id2);
        GraphEdge edge = new GraphEdge();
        edge.setAddressAId(lo);
        edge.setAddressBId(hi);
        graphEdgeRepository.save(edge);
    }

    private void saveHospital(String name, boolean adminAvailable, int totalBeds, int occupied, Address address) {
        Hospital hospital = new Hospital();
        hospital.setName(name);
        hospital.setAvailable(adminAvailable);
        hospital.setTotalBeds(totalBeds);
        hospital.setTotalOccupiedBeds(occupied);
        hospital.setAddress(address);
        hospitalRepository.save(hospital);
    }
}
