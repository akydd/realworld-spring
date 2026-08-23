package com.akydd.realworld_spring.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    final private JwtAuthenticationFilter jwtAuthenticationFilter;
    final private TokenAuthenticationEntryPoint tokenAuthenticationEntryPoint;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter, TokenAuthenticationEntryPoint tokenAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tokenAuthenticationEntryPoint = tokenAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(@NonNull HttpSecurity http) throws Exception {
        return http
                // No need for browser managed cookies
                .csrf((csrf) -> csrf.disable())

                // Session is stateless: NO COOKIES!
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Which routes needs JWTs?
                .authorizeHttpRequests((authorize) -> authorize
                        // Public routes
                        .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tags").permitAll()

                        // Optional authentication
                        .requestMatchers(HttpMethod.GET, "/api/profiles/{username}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/articles/feed").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/articles").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/articles/{slug}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/articles/{slug}/comments").permitAll()

                        // Health check
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()

                        // All others require authentication
                        .anyRequest().authenticated()
                )

                // We aren't using the UsernamePasswordAuthenticationFilter. This just amkes sure that the
                // jwtAuthenticationFilter is fired before all the other ones.
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(tokenAuthenticationEntryPoint))
                .build();
    }
}
