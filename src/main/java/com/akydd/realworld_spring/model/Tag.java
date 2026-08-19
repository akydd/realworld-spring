package com.akydd.realworld_spring.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 255)
    private String name;

    /**
     * Constructor required by JPA.
     */
    protected Tag() {
    }

    /**
     * A convenience constructor.
     *
     * @param name Unique tag name.
     */
    public Tag(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Tag t && Objects.equals(name, t.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
