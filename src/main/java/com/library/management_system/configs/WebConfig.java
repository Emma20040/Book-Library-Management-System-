//package com.library.management_system.configs;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig {
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
////                registry.addMapping("/api/**")
//                registry.addMapping("/**")
////                        .allowedOrigins("https://d0d07a23a7bb.ngrok-free.app")
//                        .allowedOrigins("*")
//                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
////                        .allowedHeaders("Content-Type", "Authorization", "Accept", "Origin")
//                       .allowCredentials(true)
//                        .allowedHeaders("*");
//            }
//        };
//    }
//}


package com.library.management_system.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

//@Configuration
//public class WebConfig {


//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                CorsConfiguration config = new CorsConfiguration();
//                registry.addMapping("/**") // Apply to ALL endpoints in the application
//                        .allowedOrigins(
//                                "http://localhost:5502"
//                        )
//                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//                        .allowedHeaders("*") // Allow all headers
//                        .allowCredentials(true);
//            }
//        };
//    }
//}
