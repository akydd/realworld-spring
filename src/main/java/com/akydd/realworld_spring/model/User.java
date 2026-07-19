package com.akydd.realworld_spring.model;

import jakarta.persistence.*;

import java.util.Optional;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String username;

    @Column(nullable = false, length = 45)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = true)
    private String bio;

    @Column(nullable = true, length = 100)
    private String image;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Optional<String> getBio() {
        return Optional.ofNullable(bio);
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Optional<String> getImage() {
        return Optional.ofNullable(image);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
