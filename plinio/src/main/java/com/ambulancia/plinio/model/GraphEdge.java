package com.ambulancia.plinio.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "graph_edges",
        uniqueConstraints = @UniqueConstraint(columnNames = {"address_a_id", "address_b_id"})
)
public class GraphEdge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address_a_id", nullable = false)
    private long addressAId;

    @Column(name = "address_b_id", nullable = false)
    private long addressBId;

    public GraphEdge() {
    }

    public GraphEdge(Long id, long addressAId, long addressBId) {
        this.id = id;
        this.addressAId = addressAId;
        this.addressBId = addressBId;
    }

    public Long getId() {
        return id;
    }

    public long getAddressAId() {
        return addressAId;
    }

    public void setAddressAId(long addressAId) {
        this.addressAId = addressAId;
    }

    public long getAddressBId() {
        return addressBId;
    }

    public void setAddressBId(long addressBId) {
        this.addressBId = addressBId;
    }
}
