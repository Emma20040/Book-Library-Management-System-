package com.library.management_system.configs;


import com.library.management_system.services.JwtValidationService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private JwtConfig jwtConfig;

    private JwtValidationService jwtValidationService;

    public SecurityConfig(JwtConfig jwtConfig, JwtValidationService jwtValidationService) {
        this.jwtValidationService= jwtValidationService;
        this.jwtConfig = jwtConfig;
}


    

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "/user/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/login").permitAll()

                        .requestMatchers(HttpMethod.POST, "/user/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/user/verify-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/user/profile").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/user/profile").authenticated()
                        .requestMatchers(HttpMethod.POST, "/user/redeem-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/user/reset-password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/user/admin/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/countUsers").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(config -> config.jwt(jwt -> jwt.decoder(jwtConfig.jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                // Add custom filter to validate tokens against blacklist
                .addFilterBefore(new JwtBlacklistFilter(jwtValidationService), BearerTokenAuthenticationFilter.class);;


        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    }



