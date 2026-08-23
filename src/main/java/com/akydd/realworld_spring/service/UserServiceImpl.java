package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.UpdateUser;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserServiceImpl(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getId());
        savedUser.setToken(token);

        return savedUser;
    }

    public User loginUser(String email, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
        Authentication auth = authenticationManager.authenticate(token);
        User user = (User) auth.getPrincipal();

        String jwtToken = jwtService.generateToken(user.getId());
        user.setToken(jwtToken);

        return user;
    }

    public User updateUser(long userId, UpdateUser updateUser) {
        User userToUpdate = userRepository.findById(userId).orElseThrow();

        // We have to check each field
        if (updateUser.email() != null) {
            userToUpdate.setEmail(updateUser.email());
        }
        if (updateUser.password() != null) {
            userToUpdate.setPassword(passwordEncoder.encode(updateUser.password()));
        }
        if (updateUser.username() != null) {
            userToUpdate.setUsername(updateUser.username());
        }

        if (updateUser.bio().isPresent()) {
            userToUpdate.setBio(updateUser.bio().get());
        }
        if (updateUser.image().isPresent()) {
            userToUpdate.setImage(updateUser.image().get());
        }

        return userRepository.save(userToUpdate);
    }
}
