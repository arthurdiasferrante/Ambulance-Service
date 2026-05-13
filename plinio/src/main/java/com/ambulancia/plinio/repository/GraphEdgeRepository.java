package com.ambulancia.plinio.repository;

import com.ambulancia.plinio.model.GraphEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GraphEdgeRepository extends JpaRepository<GraphEdge, Long> {

    boolean existsByAddressAIdAndAddressBId(long addressAId, long addressBId);

    @Modifying
    @Query("DELETE FROM GraphEdge e WHERE e.addressAId = :addressId OR e.addressBId = :addressId")
    void deleteAllIncidentTo(@Param("addressId") long addressId);
}
