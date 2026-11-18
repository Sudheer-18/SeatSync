package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "routes", uniqueConstraints = @UniqueConstraint(columnNames = {"source", "destination"}))
public class RouteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String source;
    @Column(nullable = false, length = 100)
    private String destination;

    protected RouteEntity() { }

    public RouteEntity(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    public Long getId() { return id; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public void setSource(String source) { this.source = source; }
    public void setDestination(String destination) { this.destination = destination; }
}
