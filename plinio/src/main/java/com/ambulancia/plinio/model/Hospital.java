package com.ambulancia.plinio.model;


import jakarta.persistence.*;

@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long Id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private int totalBeds;

    @Column(nullable = false)
    private int totalOccupiedBeds;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;


    public long getId() {
        return Id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getTotalBeds() {
        return totalBeds;
    }

    public void setTotalBeds(int totalBeds) {
        this.totalBeds = totalBeds;
    }

    public int getTotalOccupiedBeds() {
        return totalOccupiedBeds;
    }

    public void setTotalOccupiedBeds(int totalOccupiedBeds) {
        this.totalOccupiedBeds = totalOccupiedBeds;
    }

}
