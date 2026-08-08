package com.akydd.realworld_spring.config;

import jakarta.servlet.DispatcherType;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(@NonNull HttpSecurity http) throws Exception {
        return http
                // No need for browser managed cookies
                .csrf((csrf) -> csrf.disable())

                // Sessions is stateless
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Which routes needs JWTs?
                .authorizeHttpRequests((authorize) -> authorize
                //.requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                        // allow everyone for now
                    .requestMatchers(HttpMethod.POST, "/**").permitAll()
                )
                .build();
    }
}
