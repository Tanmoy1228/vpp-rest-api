package com.tanmoy.vpp.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "batteries")
public class Battery {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "postcode", length = 10, nullable = false)
    private String postcode;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;


    public Battery() {}

    public Battery(String name, String postcode, Integer capacity) {
        this.name = name;
        this.postcode = postcode;
        this.capacity = capacity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
