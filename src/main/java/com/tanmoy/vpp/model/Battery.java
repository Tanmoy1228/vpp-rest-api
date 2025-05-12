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

    @Column(name = "postcode_numeric", nullable = false)
    private Integer postcodeNumeric;

    public Battery() {}

    private Battery(String name, String postcode, int capacity) {
        this.name = name;
        this.capacity = capacity;
        setPostcode(postcode);
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getPostcodeNumeric() {
        return postcodeNumeric;
    }

    private void setPostcode(String postcode) {
        if (postcode == null || !postcode.matches("^\\d{4,10}$")) {
            throw new IllegalArgumentException("Postcode must be a numeric string with 4 to 10 digits");
        }
        try {
            this.postcode = postcode;
            this.postcodeNumeric = Integer.parseInt(postcode);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Postcode must be a numeric string with 4 to 10 digits", e);
        }
    }

    public static Battery of(String name, String postcode, int capacity) {
        return new Battery(name, postcode, capacity);
    }
}
