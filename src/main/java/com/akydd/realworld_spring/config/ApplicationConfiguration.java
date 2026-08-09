package com.akydd.realworld_spring.config;

import com.akydd.realworld_spring.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
public class ApplicationConfiguration {

    private final UserRepository userRepository;

    public ApplicationConfiguration(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserRepository userRepo, PasswordEncoder passwordEncoder) throws Exception {
        var provider = new DaoAuthenticationProvider((email) -> userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("bad credentials")));
        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(Collections.singletonList(provider));
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return userIdString -> {
            final long userId = Long.parseLong(userIdString);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        };
    }
}
