package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.repository.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Note that the username here is really a string representation of the user id.
     * @param userIdString the user id, as a string, identifying the user whose data is required.
     * @return the matching user
     * @throws UsernameNotFoundException when no user is found
     */
    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String userIdString) throws UsernameNotFoundException {
        Long userId = Long.valueOf(userIdString);
        return userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException(userIdString));
    }
}
