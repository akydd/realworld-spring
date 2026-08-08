package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.exception.InvalidCredentialsException;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(UserRepository userRepository, JwtService jwtService, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User registerUser(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getId());
        savedUser.setToken(token);

        return savedUser;
    }

    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtService.generateToken(user.getId());
        user.setToken(token);

        return user;
    }
}
