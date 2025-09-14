package com.library.management_system.configs;

import com.library.management_system.services.JwtValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtConfig jwtConfig;
    private final JwtValidationService jwtValidationService;

    public SecurityConfig(JwtConfig jwtConfig, JwtValidationService jwtValidationService) {
        this.jwtConfig = jwtConfig;
        this.jwtValidationService = jwtValidationService;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
                "http://127.0.0.1:5502",
                "http://localhost:5502"

//                "https://e218876891ff.ngrok-free.app"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ENDPOINTS - Endpoints which will not go through JWT validation
                        .requestMatchers(
                                "/user/register",
                                "/user/login",
                                "/user/verify-email",
                                "/user/redeem-password",
                                "/user/reset-password",
                                "/api/books/search",
                                "/webhook/payment"

                        ).permitAll()

                        // AUTHENTICATED ENDPOINTS
                        .requestMatchers(HttpMethod.POST, "/user/logout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/user/profile").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/user/profile").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/payments/create").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/access/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/transactions").authenticated()

                        // ADMIN ENDPOINTS
                        .requestMatchers("/user/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/books/genre").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/books/countBooks").hasRole("ADMIN")
                        .requestMatchers("/countUsers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/user/admin/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/user/admin/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/user/admin/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "api/payments/countTotalTransactions").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtConfig.jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )

                .addFilterBefore(new JwtBlacklistFilter(jwtValidationService), UsernamePasswordAuthenticationFilter.class);

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