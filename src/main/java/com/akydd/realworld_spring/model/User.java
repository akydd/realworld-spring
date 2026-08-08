package com.akydd.realworld_spring.model;

import jakarta.persistence.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45, unique = true)
    private String username;

    @Column(nullable = false, length = 45, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = true)
    private String bio;

    @Column(nullable = true, length = 100)
    private String image;

    @Transient
    private String token;

    public Long getId() { return id; }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
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

    /**
     * Note that this returns the id, not the username, as the username is mutable,
     * but the id is not.
     */
    @Override
    @NullMarked
    public String getUsername() {
        return String.valueOf(id);
    }

    // Needed due to the above override for the UserDetails interface.
    public String getRealUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
